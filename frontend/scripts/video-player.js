class HLSPlayer {
  constructor(videoElement, masterPlaylistUrl) {
    this.video = videoElement;
    this.hls = null;
    this.levels = [];
    this.currentLevel = -1; // -1 = auto
    
    this.init(masterPlaylistUrl);
  }

  init(masterPlaylistUrl) {
    if (Hls.isSupported()) {
      this.hls = new Hls({
        debug: false,
        enableWorker: true,
        autoStartLoad: true,
        startLevel: -1,
        capLevelToPlayerSize: true,
        maxBufferLength: 30,
        maxMaxBufferLength: 60
      });
      
      this.hls.loadSource(masterPlaylistUrl);
      this.hls.attachMedia(this.video);
      
      this.hls.on(Hls.Events.MANIFEST_PARSED, (event, data) => {
        console.log('Manifest loaded with', data.levels.length, 'quality levels');
        this.setupQualitySelector(data.levels);
      });
      
      this.hls.on(Hls.Events.LEVEL_SWITCHED, (event, data) => {
        this.updateQualityDisplay(data.level);
      });
      
      this.hls.on(Hls.Events.ERROR, (event, data) => {
          this.handleError(data);
      });
    } else if (this.video.canPlayType('application/vnd.apple.mpegurl')) {
      this.video.src = masterPlaylistUrl;
    } else {
      alert('HLS is not supported in your browser');
    }
  }

  setupQualitySelector(levels) {
    const selector = document.getElementById('quality-selector');
    
    levels.forEach((level, index) => {
      const option = document.createElement('option');
      option.value = index;
      option.textContent = `${level.height}p (${(level.bitrate / 1000).toFixed(0)} kbps)`;
      selector.appendChild(option);
    });
    
    selector.addEventListener('change', (e) => {
      const level = parseInt(e.target.value);
      this.setQuality(level);
    });
  }

  setQuality(levelIndex) {
    if (this.hls) {
      this.hls.currentLevel = levelIndex;
      this.currentLevel = levelIndex;
    }
  }

  updateQualityDisplay(levelIndex) {
    const display = document.getElementById('current-quality');
    if (levelIndex === -1) {
      display.textContent = 'Auto';
    } else if (this.hls && this.hls.levels[levelIndex]) {
      display.textContent = `${this.hls.levels[levelIndex].height}p`;
    }
  }

  handleError(data) {
    if (data.fatal) {
      switch(data.type) {
        case Hls.ErrorTypes.NETWORK_ERROR:
          console.error('Fatal network error');
          this.hls.startLoad();
          break;
        case Hls.ErrorTypes.MEDIA_ERROR:
          console.error('Fatal media error');
          this.hls.recoverMediaError();
          break;
        default:
          console.error('Fatal error, destroying player');
          this.destroy();
          break;
      }
    }
  }

  destroy() {
    if (this.hls) {
      this.hls.destroy();
      this.hls = null;
    }
  }

  getStats() {
    if (this.hls) {
      return {
        currentLevel: this.hls.currentLevel,
        levels: this.hls.levels,
        bandwidthEstimate: this.hls.bandwidthEstimate
      };
    }
    return null;
  }
}

document.addEventListener("DOMContentLoaded", async () => {
  const video = document.getElementById('video');
  const urlParams = new URLSearchParams(window.location.search);
  const videoID = urlParams.get('videoID');

  const masterPlaylistUrl = `http://localhost:8080/video/${videoID}/master.m3u8`;

  fetch(masterPlaylistUrl, {
    method: "GET",
    credentials: "include",
  })
  .then(response => console.log(response));

  const player = new HLSPlayer(video, masterPlaylistUrl);
  window.player = player;
});