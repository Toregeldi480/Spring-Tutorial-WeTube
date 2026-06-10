function createVideoListItem(videoId, thumbanilUrl, title, description) {
  const videoLink = document.createElement("a");
  const videoLinkLeft = document.createElement("div");
  const videoThumbnailContainer = document.createElement("div");
  const videoThumbnail = document.createElement("img");
  const videoLinkRight = document.createElement("div");
  const videoTitleContainer = document.createElement("div");
  const videoTitle = document.createElement("p");
  const videoDescriptionContainer = document.createElement("div");
  const videoDescription = document.createElement("p");

  videoLink.classList.add("video-link");
  videoLinkLeft.classList.add("video-link-left");
  videoThumbnailContainer.classList.add("video-thumbnail-container");
  videoThumbnail.classList.add("video-thumbnail");
  videoLinkRight.classList.add("video-link-right");
  videoTitleContainer.classList.add("video-title-container");
  videoTitle.classList.add("video-title");
  videoDescriptionContainer.classList.add("video-description-container");
  videoDescription.classList.add("video-description");

  videoLink.href = `video.html?videoID=${videoId}`;
  videoThumbnail.src = thumbanilUrl;
  videoTitle.innerText = title;
  videoDescription.innerText = description;

  videoThumbnailContainer.appendChild(videoThumbnail);
  videoTitleContainer.appendChild(videoTitle);
  videoDescriptionContainer.appendChild(videoDescription);

  videoLinkLeft.appendChild(videoThumbnailContainer);
  videoLinkRight.appendChild(videoTitleContainer);
  videoLinkRight.appendChild(videoDescriptionContainer);

  videoLink.appendChild(videoLinkLeft);
  videoLink.appendChild(videoLinkRight);

  const list = document.getElementById("video-list");
  list.append(videoLink);
}