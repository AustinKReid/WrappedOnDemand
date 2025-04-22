//Function to grab the data from java
function fetchDataUntilReady() {
    fetch('/data')
        .then(res => res.json())
        .then(data => {
            if (data.top50) {
                document.getElementById('message').textContent = data.top50;
            } else {
                setTimeout(fetchDataUntilReady, 500);
            }
        })
        .catch(err => {
            console.error('Error:', err);
            setTimeout(fetchDataUntilReady, 1000);
        });
}

fetchDataUntilReady();
