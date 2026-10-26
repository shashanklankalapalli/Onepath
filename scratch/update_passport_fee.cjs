const { execFile } = require('child_process');
const psqlPath = 'C:\\Program Files\\PostgreSQL\\18\\bin\\psql.exe';
const env = { ...process.env, PGPASSWORD: 'qwe' };

const sql = `
UPDATE services 
SET fees = 'Normal: ' || chr(8377) || '2,500 (36 pages) / ' || chr(8377) || '3,500 (60 pages) | Minor: ' || chr(8377) || '1,750 | Lost/Damaged: ' || chr(8377) || '5,000 / ' || chr(8377) || '6,000 | Tatkaal: ' || chr(8377) || '5,000 (36 pages) / ' || chr(8377) || '6,000 (60 pages) [Replacement: ' || chr(8377) || '7,500 / ' || chr(8377) || '8,500] | Fee Calculator: https://www.passportindia.gov.in/psp/FeeCalculator'
WHERE slug = 'passport-application-renewal';
`;

execFile(psqlPath, ['-U', 'postgres', '-d', 'onepath_db', '-c', sql], { env }, (err, stdout, stderr) => {
  if (err) {
    console.error('Error executing query:', err);
    process.exit(1);
  }
  console.log('Update result:\n', stdout);
  if (stderr) console.error('STDERR:\n', stderr);
});
