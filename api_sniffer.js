const puppeteer = require('puppeteer');
const fs = require('fs');

(async () => {
    // Launch browser with head visible so user can interact
    const browser = await puppeteer.launch({
        headless: false,
        args: ['--no-sandbox', '--disable-setuid-sandbox']
    });
    const page = await browser.newPage();

    // Enable request interception
    await page.setRequestInterception(true);

    const requests = [];

    page.on('request', request => {
        const url = request.url();
        // Filter for API calls (ignoring static assets mostly)
        if (url.includes('ircc') || url.includes('cic.gc.ca')) {
            if (['POST', 'PUT', 'GET'].includes(request.method())) {
                console.log(`[${request.method()}] ${url}`);
                requests.push({
                    method: request.method(),
                    url: url,
                    headers: request.headers(),
                    postData: request.postData()
                });
            }
        }
        request.continue();
    });

    console.log('Navigate to login page...');
    await page.goto('https://ircc-tracker-suivi.apps.cic.gc.ca/en/login', { waitUntil: 'networkidle2' });

    console.log('Waiting for user interaction... PLEASE LOGIN MANUALLY.');
    
    // We wait for a long time to allow the user to perform login
    // The script will keep logging requests in the background
    await new Promise(r => setTimeout(r, 60000)); // Wait 60 seconds

    console.log('Saving captured requests to captured_requests.json');
    fs.writeFileSync('captured_requests.json', JSON.stringify(requests, null, 2));

    await browser.close();
})();
