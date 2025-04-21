console.log("Page loaded. Fetching data...");

function fetchDataUntilReady() {
    fetch('http://localhost:3000/data')
        .then(res => {
            console.log("Response received:", res);
            return res.json();
        })
        .then(data => {
            console.log("Data received:", data);
            if (data.top50 && data.top50.trim() !== "") {
                document.getElementById('message').textContent = data.top50;
            } else {
                console.log("Data empty, retrying...");
                setTimeout(fetchDataUntilReady, 500);
            }
        })
        .catch(err => {
            console.error('Error fetching /data:', err);
            setTimeout(fetchDataUntilReady, 1000);
        });
}

fetchDataUntilReady();