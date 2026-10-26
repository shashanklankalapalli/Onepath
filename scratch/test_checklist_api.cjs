const http = require('http');

function post(url, data, token) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const body = JSON.stringify(data);
    const headers = {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(body),
    };
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const req = http.request(
      {
        hostname: u.hostname,
        port: u.port,
        path: u.pathname + u.search,
        method: 'POST',
        headers,
      },
      (res) => {
        let resBody = '';
        res.on('data', (c) => (resBody += c));
        res.on('end', () => {
          resolve({ status: res.statusCode, data: resBody });
        });
      }
    );
    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

function get(url, token) {
  return new Promise((resolve, reject) => {
    const u = new URL(url);
    const headers = {};
    if (token) headers['Authorization'] = 'Bearer ' + token;

    const req = http.request(
      {
        hostname: u.hostname,
        port: u.port,
        path: u.pathname + u.search,
        method: 'GET',
        headers,
      },
      (res) => {
        let resBody = '';
        res.on('data', (c) => (resBody += c));
        res.on('end', () => {
          resolve({ status: res.statusCode, data: resBody });
        });
      }
    );
    req.on('error', reject);
    req.end();
  });
}

async function run() {
  console.log('Testing login...');
  const loginRes = await post('http://localhost:8080/api/auth/login', {
    email: 'student@onepath.org',
    password: 'student123',
  });
  console.log('Login status:', loginRes.status);
  if (loginRes.status !== 200) {
    console.error('Login failed:', loginRes.data);
    return;
  }
  const loginData = JSON.parse(loginRes.data);
  const token = loginData.accessToken || loginData.token;
  console.log('Got token:', token ? 'YES' : 'NO');

  console.log('\nTesting POST /api/checklists (Personal Document)...');
  const saveRes = await post(
    'http://localhost:8080/api/checklists',
    {
      targetType: 'PERSONAL',
      targetId: 0,
      documentName: 'Income Certificate (Tahsildar)',
      completed: false,
      isMissing: false,
    },
    token
  );
  console.log('Save checklist status:', saveRes.status);
  console.log('Save response:', saveRes.data);

  console.log('\nTesting GET /api/checklists?targetType=PERSONAL...');
  const getRes = await get('http://localhost:8080/api/checklists?targetType=PERSONAL', token);
  console.log('Get personal checklist status:', getRes.status);
  console.log('Get response:', getRes.data);
}

run().catch(console.error);
