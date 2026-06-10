const upload= document.getElementById("upload");
const file= document.getElementById("file");
const title = document.getElementById("title");
const description = document.getElementById("description");
const uploadCancel = document.getElementById("upload_cancel");
const uploadBlock = document.getElementById("upload_block");

const backendUrl = "http://localhost:8080";
const videoUrl = `${backendUrl}/video`;

document.getElementById("upload_form").addEventListener("submit", async (e) => {
  e.preventDefault();
  
  const formData = new FormData(e.target);
  
  const response = await fetch(`${videoUrl}/upload`, {
    method: "POST",
    credentials: "include",
    body: formData,
  })
  .then(response => response.json())
  .then(json => {
    console.log(json);
  })
})

upload.addEventListener("click", () => {
  uploadBlock.style.display = "flex";
});

uploadCancel.addEventListener("click", () => {
  uploadBlock.style.display = "none";
});
