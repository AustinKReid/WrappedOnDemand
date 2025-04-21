function fetchDataUntilReady() {
    fetch('http://localhost:3000/data')
        .then(res => {
            return res.json();
        })
        .then(data => {
            if (data.top50 && data.top50.trim() !== "") {
                document.getElementById('message').textContent = data.top50;
            } else {
                setTimeout(fetchDataUntilReady, 500);
            }
        })
        .catch(err => {
            setTimeout(fetchDataUntilReady, 1000);
        });
}

fetchDataUntilReady();