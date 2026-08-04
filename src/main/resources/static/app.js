const form = document.querySelector("#shorten-form");
const longUrlInput = document.querySelector("#long-url");
const submitButton = document.querySelector("#submit-button");
const statusMessage = document.querySelector("#status");
const result = document.querySelector("#result");
const shortUrlInput = document.querySelector("#short-url");
const copyButton = document.querySelector("#copy-button");

form.addEventListener("submit", async (event) => {
    event.preventDefault();

    const longUrl = longUrlInput.value.trim();
    if (!longUrl) {
        setStatus("Enter a URL first.", true);
        return;
    }

    setLoading(true);
    setStatus("Shortening...");
    result.hidden = true;

    try {
        const response = await fetch("/v1/shorten", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({ longUrl })
        });

        if (!response.ok) {
            throw new Error(await errorMessageFor(response));
        }

        const data = await response.json();
        shortUrlInput.value = data.shortUrl;
        result.hidden = false;
        setStatus("Short link created.");
    } catch (error) {
        setStatus(error.message || "Could not shorten the URL.", true);
    } finally {
        setLoading(false);
    }
});

copyButton.addEventListener("click", async () => {
    if (!shortUrlInput.value) {
        return;
    }

    await navigator.clipboard.writeText(shortUrlInput.value);
    setStatus("Copied.");
});

function setLoading(isLoading) {
    submitButton.disabled = isLoading;
    submitButton.textContent = isLoading ? "Working" : "Shorten";
}

function setStatus(message, isError = false) {
    statusMessage.textContent = message;
    statusMessage.classList.toggle("error", isError);
}

async function errorMessageFor(response) {
    try {
        const data = await response.json();
        return data.detail || data.message || `Request failed with status ${response.status}.`;
    } catch {
        return `Request failed with status ${response.status}.`;
    }
}
