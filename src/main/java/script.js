
fetch('/data')
.then(response => response.json())
.then(data => {
document.getElementById('message').textContent = data;
console.log('Received:', data);
})
                    .catch(error => console.error('Error:', error));
            """;