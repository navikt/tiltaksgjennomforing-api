/*
 * Script for å konvertere en H2-dump til en SQL-fil som kan brukes i labs.
 * Denne vil fjerne tokens som ikke er nødvendige i tillegg til at den vil konvertere
 * absolutt tid til relativt format.
 *
 * Fremgangsmåte:
 * 1. Kjør opp tiltaksgjennomføring og skriv "SCRIPT TO 'dump.sql';" i H2-konsollen.
 * 2. Kjør dette scriptet i terminal "node ./after-migrate-labs.js"
 */

const fs = require('node:fs');

const TWENTY_FOUR_HOURS_IN_MS = 24 * 60 * 60 * 1000;

const getProgressBar = (total) => {
    return (current) => {
        if (current % 100 !== 0) {
            return;
        }

        const progress = current/total < 0.95 ? current/total : 1;
        const barWidth = 30;
        const filledWidth = Math.floor(progress * barWidth);
        const emptyWidth = barWidth - filledWidth;
        const progressBar = '█'.repeat(filledWidth) + '▒'.repeat(emptyWidth);

        process.stdout.clearLine();
        process.stdout.cursorTo(0);
        process.stdout.write(`[${progressBar}] ${Math.round(progress * 100)}%`);
    };
};

const replaceDatetime = (str) => {
    const date = new Date(str.replace('TIMETAMP ', '').replaceAll('\'', ''));
    const diff = date.setHours(0,0,0,0) - new Date().setHours(0,0,0,0);

    if (diff === 0) {
        return 'CURRENT_TIMESTAMP';
    }

    const daysBetween = Math.round(diff / TWENTY_FOUR_HOURS_IN_MS);
    return `DATEADD('DAY', ${daysBetween}, CURRENT_TIMESTAMP)`;
};

const replaceDate = (str) => {
    const date = new Date(str.replace('DATE ', '').replaceAll('\'', ''));
    const diff = date.setHours(0,0,0,0) - new Date().setHours(0,0,0,0);

    if (diff === 0) {
        return 'CURRENT_DATE';
    }

    const daysBetween = Math.round(diff / TWENTY_FOUR_HOURS_IN_MS);
    return `DATEADD('DAY', ${daysBetween}, CURRENT_DATE)`;
};

try {
    console.log('\n **** 🚀 LABS MIGRERING 🚀 **** \n');
    const file = fs.readFileSync('./dump.sql', 'utf8');

    const sisteLinjeMedFlyway = file.split('\n')
        .map((linje, idx) => linje.includes('flyway_schema_history_s_idx') ? idx : -1)
        .filter((idx) => idx !== -1)[0] || -1;

    const fileArr = file.split('\n').filter((_, idx) => idx > sisteLinjeMedFlyway);
    const drawProgressBar = getProgressBar(fileArr.length);
    const output = fs.createWriteStream('./src/main/resources/db/callbacks/labs/afterMigrate__sql-dump.sql');

    fileArr.forEach((line, idx) => {
        [...line.matchAll(/TIMESTAMP '20\d{2}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(\.\d*)?'/g)].forEach((match) => {
            line = line.replace(match[0], replaceDatetime(match[0]));
        });
        [...line.matchAll(/DATE '20\d{2}-\d{2}-\d{2}'/g)].forEach((match) => {
            line = line.replace(match[0], replaceDate(match[0]));
        });

        line.replaceAll(/MEMORY /g, '');
        line.replaceAll(/"PUBLIC"\./g, '');

        output.write(line + '\n');
        drawProgressBar(idx);
    });

    output.end();
    console.log('\n\n✅  Ferdig!')
} catch (err) {
    console.error(err);
}
