fetch('/data')
    .then(response => response.json())
    .then(data => {
        document.getElementById('message').textContent = data.top50;
        console.log('Received:', data.top50);
    })
    .catch(error => console.error('Error fetching top 50:', error));