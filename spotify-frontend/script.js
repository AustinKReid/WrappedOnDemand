// This simulates the kind of data the backend will send
const fakeSpotifyData = [
  { name: "All I Want For Christmas Is You", artist: "Mariah Carey" },
  { name: "Ocean Man", artist: "Ween" },
  { name: "Thick Of It", artist: "KSI" }
];

// Function to show that data on the page
function displayTracks(tracks) {
  const container = document.getElementById("tracks-container");
  container.innerHTML = "";

  tracks.forEach(track => {
    const div = document.createElement("div");
    div.className = "track";
    div.textContent = `${track.name} by ${track.artist}`;
    container.appendChild(div);
  });
}

// For now, just show the fake data
displayTracks(fakeSpotifyData);