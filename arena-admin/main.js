(function () {
    const baseUrl = 'http://localhost:12345/tiltaksgjennomforing-api/utvikler-admin/arena'

    const getToken = () => {
        const tokenInput = document.getElementById('token');
        return tokenInput.value;
    };

    const getStatistikk = () => {
        const options = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`,
            }
        };

        return fetch(`${baseUrl}/tiltak/MENTOR/statistikk`, options)
            .then(response => response.json())
            .catch(error => {
                alert('Feil ved henting av statistikk: ' + error)
            });
    }

    const reset = () => {
        const options = {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${getToken()}`,
            }
        };

        return fetch(`${baseUrl}/tiltak/MENTOR/reset`, options)
            .catch(error => {
                alert('Feil ved tilbakestilling: ' + error);
            });
    }

    const run = async () => {
        if (!getToken()) {
            alert('Vennligst oppgi token');
            return;
        }

        const { status, statistikk: { PROCESSING = 0, COMPLETED, FAILED } }  = await getStatistikk();

        const spinner = document.getElementById('spinner');
        const statusMigration = document.getElementById('status-migration');
        if (status) {
            spinner.style.display = 'none';
            statusMigration.style.display = 'block';
            statusMigration.innerHTML = status === 'Ferdig migrert' ? `<b>✅ ${status}</b>` : `<b>🚀 ${status}</b>`;
        } else {
            spinner.style.display = 'block';
            statusMigration.style.display = 'none';
        }

        const statusProcessing = document.getElementById('status-processing');
        if (PROCESSING) {
            statusProcessing.style.display = 'block';
            statusProcessing.innerHTML = `♻️ <b>Prosseseserer:</b> ${PROCESSING ?? 0}`;
        } else {
            statusProcessing.style.display = 'none';
        }

        if (COMPLETED) {
            const completedTable = document.getElementById('completed-table');
            completedTable.style.display = 'block';

            const completedTableBody = document.getElementById('completed-table-body');
            completedTableBody.innerHTML = '';

            Object.entries(COMPLETED).forEach(([key, value]) => {
                const row = document.createElement('tr');
                row.innerHTML = `
                <td>${key}</td>
                <td>${value}</td>
            `;
                completedTableBody.appendChild(row);
            });
        }
        if (FAILED) {
            const failedTable = document.getElementById('failed-table');
            failedTable.style.display = 'block';

            const failedTableBody = document.getElementById('failed-table-body');
            failedTableBody.innerHTML = '';

            Object.entries(FAILED).forEach(([key, value]) => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${key}</td>
                    <td>${value}</td>
                `;
                failedTableBody.appendChild(row);
            });

            const resetButton = document.getElementById('reset-button');
            resetButton.removeEventListener('click', reset);
            resetButton.addEventListener('click', reset);
        }
    };

    setInterval(run, 10000);
})();
