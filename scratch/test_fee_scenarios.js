const http = require('http');

const scenarios = [
  {
    name: '1. Adult Fresh + Normal + 36 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'ADULT',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_36',
      educationLevel: 'NON_ECR_QUALIFIED'
    },
    expectedFee: '₹2,500'
  },
  {
    name: '2. Adult Fresh + Normal + 60 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'ADULT',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_60',
      educationLevel: 'NON_ECR_QUALIFIED'
    },
    expectedFee: '₹3,500'
  },
  {
    name: '3. Adult Fresh + Tatkaal + 36 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'ADULT',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_36',
      educationLevel: 'NON_ECR_QUALIFIED'
    },
    expectedFee: '₹5,000'
  },
  {
    name: '4. Adult Fresh + Tatkaal + 60 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'ADULT',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_60',
      educationLevel: 'NON_ECR_QUALIFIED'
    },
    expectedFee: '₹6,000'
  },
  {
    name: '5. Minor under 18 + Normal + 36 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'MINOR',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_36'
    },
    expectedFee: '₹1,750'
  },
  {
    name: '6. Minor under 18 + Tatkaal + 36 pages',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'MINOR',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_36'
    },
    expectedFee: '₹4,250'
  },
  {
    name: '7. Adult Renewal/Reissue + Normal + 36 pages',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_36',
      renewalReason: 'EXPIRY_PAGES_NO_CHANGE'
    },
    expectedFee: '₹2,500'
  },
  {
    name: '8. Adult Renewal/Reissue + Tatkaal + 36 pages',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_36',
      renewalReason: 'EXPIRY_PAGES_NO_CHANGE'
    },
    expectedFee: '₹5,000'
  },
  {
    name: '9. Lost/Damaged Replacement Adult 36p Normal',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_36',
      renewalReason: 'LOST_DAMAGED'
    },
    expectedFee: '₹5,000'
  },
  {
    name: '10. Lost/Damaged Replacement Adult 36p Tatkaal',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_36',
      renewalReason: 'LOST_DAMAGED'
    },
    expectedFee: '₹7,500'
  },
  {
    name: '11. Lost/Damaged Replacement Adult 60p Normal',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_60',
      renewalReason: 'LOST_DAMAGED'
    },
    expectedFee: '₹6,000'
  },
  {
    name: '12. Lost/Damaged Replacement Adult 60p Tatkaal',
    payload: {
      applicationType: 'RENEWAL',
      applicantCategory: 'ADULT',
      scheme: 'TATKAAL',
      bookletSize: 'PAGES_60',
      renewalReason: 'LOST_DAMAGED'
    },
    expectedFee: '₹8,500'
  },
  {
    name: '13. Unlisted Category: Police Clearance Certificate (PCC)',
    payload: {
      applicationType: 'PCC'
    },
    expectedFee: 'Refer to Official Fee Calculator'
  },
  {
    name: '14. Unlisted Category: Minor with 60 Pages Jumbo Booklet',
    payload: {
      applicationType: 'FRESH',
      applicantCategory: 'MINOR',
      scheme: 'NORMAL',
      bookletSize: 'PAGES_60'
    },
    expectedFee: 'Refer to Official Fee Calculator'
  }
];

function testScenario(s) {
  return new Promise((resolve) => {
    const postData = JSON.stringify(s.payload);
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/services/passport-application-renewal/evaluate',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
      }
    };

    const req = http.request(options, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        try {
          const json = JSON.parse(data);
          const fee = json.calculatedFee;
          const passed = (fee === s.expectedFee);
          console.log(`[${passed ? 'PASS' : 'FAIL'}] ${s.name}: Expected ${s.expectedFee}, Got ${fee} (Breakdown: ${json.feeBreakdown})`);
          resolve(passed);
        } catch (e) {
          console.error(`Error parsing response for ${s.name}:`, data);
          resolve(false);
        }
      });
    });

    req.on('error', (e) => {
      console.error(`Request error for ${s.name}:`, e.message);
      resolve(false);
    });

    req.write(postData);
    req.end();
  });
}

async function run() {
  console.log('--- Testing Passport Fee Evaluation Scenarios ---');
  let passed = 0;
  for (const s of scenarios) {
    const ok = await testScenario(s);
    if (ok) passed++;
  }
  console.log(`\nSummary: ${passed} / ${scenarios.length} scenarios passed.`);
}

run();
