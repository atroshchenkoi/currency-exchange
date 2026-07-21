const CURRENCIES_API_URL = "currencies";
const EXCHANGE_RATES_API_URL = "exchangeRates";
const EXCHANGE_RATE_API_URL = "exchangeRate";

const currenciesTableBody = document.querySelector("#currencies-table-body");
const currenciesStatusElement = document.querySelector("#status");
const currencyDialog = document.querySelector("#currency-dialog");
const currencyForm = document.querySelector("#currency-form");
const currencyFormError = document.querySelector("#form-error");
const currencySubmitButton = document.querySelector("#submit-button");

const exchangeRatesTableBody = document.querySelector("#exchange-rates-table-body");
const exchangeRatesStatusElement = document.querySelector("#exchange-rates-status");
const exchangeRateDialog = document.querySelector("#exchange-rate-dialog");
const exchangeRateForm = document.querySelector("#exchange-rate-form");
const exchangeRateFormError = document.querySelector("#exchange-rate-form-error");
const exchangeRateSubmitButton = document.querySelector("#submit-exchange-rate-button");
const exchangeRateSearchForm = document.querySelector("#exchange-rate-search-form");
const editExchangeRateDialog = document.querySelector("#edit-exchange-rate-dialog");
const editExchangeRateForm = document.querySelector("#edit-exchange-rate-form");
const editExchangeRatePair = document.querySelector("#edit-exchange-rate-pair");
const editExchangeRateError = document.querySelector("#edit-exchange-rate-form-error");
const editExchangeRateSubmitButton = document.querySelector("#submit-edit-exchange-rate-button");
let editingExchangeRate = null;
const exchangeForm = document.querySelector("#exchange-form");
const exchangeSubmitButton = document.querySelector("#exchange-submit-button");
const exchangeError = document.querySelector("#exchange-error");
const exchangeResult = document.querySelector("#exchange-result");
const exchangeResultValue = document.querySelector("#exchange-result-value");
const exchangeResultRate = document.querySelector("#exchange-result-rate");

document.querySelector("#open-dialog-button").addEventListener("click", openCurrencyDialog);
document.querySelector("#close-dialog-button").addEventListener("click", closeCurrencyDialog);
document.querySelector("#cancel-button").addEventListener("click", closeCurrencyDialog);
document.querySelector("#reload-button").addEventListener("click", loadCurrencies);
currencyForm.addEventListener("submit", createCurrency);

document.querySelector("#open-exchange-rate-dialog-button").addEventListener("click", openExchangeRateDialog);
document.querySelector("#close-exchange-rate-dialog-button").addEventListener("click", closeExchangeRateDialog);
document.querySelector("#cancel-exchange-rate-button").addEventListener("click", closeExchangeRateDialog);
document.querySelector("#reload-exchange-rates-button").addEventListener("click", loadExchangeRates);
document.querySelector("#reset-exchange-rate-search-button").addEventListener("click", resetExchangeRateSearch);
exchangeRateForm.addEventListener("submit", createExchangeRate);
exchangeRateSearchForm.addEventListener("submit", findExchangeRate);
document.querySelector("#close-edit-exchange-rate-dialog-button").addEventListener("click", closeEditExchangeRateDialog);
document.querySelector("#cancel-edit-exchange-rate-button").addEventListener("click", closeEditExchangeRateDialog);
editExchangeRateForm.addEventListener("submit", updateExchangeRate);
exchangeForm.addEventListener("submit", exchangeCurrency);

loadCurrencies();
loadExchangeRates();

async function loadCurrencies() {
    setStatus(currenciesStatusElement, "Загрузка списка валют…");
    currenciesTableBody.replaceChildren();

    try {
        const currencies = await fetchJson(CURRENCIES_API_URL);
        currenciesTableBody.replaceChildren(...currencies.map(createCurrencyRow));
        setStatus(currenciesStatusElement, currencies.length === 0 ? "Список валют пока пуст." : "");
    } catch (error) {
        setStatus(currenciesStatusElement, error.message, true);
    }
}

async function createCurrency(event) {
    event.preventDefault();
    currencyFormError.textContent = "";
    currencySubmitButton.disabled = true;

    try {
        await postForm(CURRENCIES_API_URL, currencyForm);
        currencyForm.reset();
        closeCurrencyDialog();
        await loadCurrencies();
    } catch (error) {
        currencyFormError.textContent = error.message;
    } finally {
        currencySubmitButton.disabled = false;
    }
}

async function loadExchangeRates() {
    setStatus(exchangeRatesStatusElement, "Загрузка списка обменных курсов…");
    exchangeRatesTableBody.replaceChildren();

    try {
        const exchangeRates = await fetchJson(EXCHANGE_RATES_API_URL);
        renderExchangeRates(exchangeRates);
        setStatus(exchangeRatesStatusElement, exchangeRates.length === 0 ? "Список обменных курсов пока пуст." : "");
    } catch (error) {
        setStatus(exchangeRatesStatusElement, error.message, true);
    }
}

async function findExchangeRate(event) {
    event.preventDefault();
    const formData = new FormData(exchangeRateSearchForm);
    const baseCode = formData.get("baseCurrencyCode").trim().toUpperCase();
    const targetCode = formData.get("targetCurrencyCode").trim().toUpperCase();

    setStatus(exchangeRatesStatusElement, "Поиск обменного курса…");
    exchangeRatesTableBody.replaceChildren();

    try {
        const exchangeRate = await fetchJson(`${EXCHANGE_RATE_API_URL}/${baseCode}${targetCode}`);
        renderExchangeRates([exchangeRate]);
        setStatus(exchangeRatesStatusElement, "");
    } catch (error) {
        setStatus(exchangeRatesStatusElement, error.message, true);
    }
}

async function createExchangeRate(event) {
    event.preventDefault();
    exchangeRateFormError.textContent = "";
    exchangeRateSubmitButton.disabled = true;

    try {
        await postForm(EXCHANGE_RATES_API_URL, exchangeRateForm);
        exchangeRateForm.reset();
        closeExchangeRateDialog();
        await loadExchangeRates();
    } catch (error) {
        exchangeRateFormError.textContent = error.message;
    } finally {
        exchangeRateSubmitButton.disabled = false;
    }
}

async function exchangeCurrency(event) {
    event.preventDefault();
    exchangeError.textContent = "";
    exchangeResult.hidden = true;
    exchangeSubmitButton.disabled = true;

    try {
        const formData = new FormData(exchangeForm);
        const parameters = new URLSearchParams(formData);
        const result = await fetchJson(`exchange?${parameters}`);

        exchangeResultValue.textContent = `${result.convertedAmount} ${result.targetCurrency.code}`;
        exchangeResultRate.textContent = `${result.amount} ${result.baseCurrency.code} × ${result.rate}`;
        exchangeResult.hidden = false;
    } catch (error) {
        exchangeError.textContent = error.message;
    } finally {
        exchangeSubmitButton.disabled = false;
    }
}

async function fetchJson(url) {
    const response = await fetch(url);
    return readResponse(response);
}

async function postForm(url, form) {
    const response = await fetch(url, {
        method: "POST",
        headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
        body: new URLSearchParams(new FormData(form))
    });
    return readResponse(response);
}

async function readResponse(response) {
    const body = await response.json();
    if (!response.ok) {
        throw new Error(body.message || "Request failed.");
    }
    return body;
}

function renderExchangeRates(exchangeRates) {
    exchangeRatesTableBody.replaceChildren(...exchangeRates.map(createExchangeRateRow));
}

function createCurrencyRow(currency) {
    return createRow([currency.code, currency.name, currency.sign]);
}

function createExchangeRateRow(exchangeRate) {
    const row = createRow([
        exchangeRate.baseCurrency.code,
        exchangeRate.targetCurrency.code,
        exchangeRate.rate
    ]);
    const actionsCell = document.createElement("td");
    actionsCell.className = "row-actions";

    const editButton = document.createElement("button");
    editButton.className = "table-action table-action-edit";
    editButton.type = "button";
    editButton.textContent = "Edit";
    editButton.addEventListener("click", () => openEditExchangeRateDialog(exchangeRate));

    const deleteButton = document.createElement("button");
    deleteButton.className = "table-action table-action-delete";
    deleteButton.type = "button";
    deleteButton.textContent = "Delete";
    deleteButton.addEventListener("click", () => deleteExchangeRate(exchangeRate));

    actionsCell.append(editButton, deleteButton);
    row.append(actionsCell);
    return row;
}

function createRow(values) {
    const row = document.createElement("tr");
    values.forEach((value) => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.append(cell);
    });
    return row;
}

function openCurrencyDialog() {
    currencyFormError.textContent = "";
    currencyDialog.showModal();
    document.querySelector("#name").focus();
}

function closeCurrencyDialog() {
    currencyDialog.close();
}

function openExchangeRateDialog() {
    exchangeRateFormError.textContent = "";
    exchangeRateDialog.showModal();
    document.querySelector("#new-base-currency-code").focus();
}

function closeExchangeRateDialog() {
    exchangeRateDialog.close();
}

function openEditExchangeRateDialog(exchangeRate) {
    editingExchangeRate = exchangeRate;
    editExchangeRateError.textContent = "";
    editExchangeRatePair.textContent = `${exchangeRate.baseCurrency.code} → ${exchangeRate.targetCurrency.code}`;
    document.querySelector("#edit-rate").value = exchangeRate.rate;
    editExchangeRateDialog.showModal();
    document.querySelector("#edit-rate").focus();
}

function closeEditExchangeRateDialog() {
    editingExchangeRate = null;
    editExchangeRateDialog.close();
}

async function updateExchangeRate(event) {
    event.preventDefault();
    editExchangeRateError.textContent = "";
    editExchangeRateSubmitButton.disabled = true;
    const rate = new URLSearchParams(new FormData(editExchangeRateForm));
    const pair = `${editingExchangeRate.baseCurrency.code}${editingExchangeRate.targetCurrency.code}`;

    try {
        const response = await fetch(`${EXCHANGE_RATE_API_URL}/${pair}`, {
            method: "PATCH",
            headers: {"Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"},
            body: rate
        });
        await readResponse(response);
        closeEditExchangeRateDialog();
        await loadExchangeRates();
    } catch (error) {
        editExchangeRateError.textContent = error.message;
    } finally {
        editExchangeRateSubmitButton.disabled = false;
    }
}

async function deleteExchangeRate(exchangeRate) {
    const pair = `${exchangeRate.baseCurrency.code}${exchangeRate.targetCurrency.code}`;
    if (!confirm(`Delete exchange rate ${exchangeRate.baseCurrency.code} → ${exchangeRate.targetCurrency.code}?`)) {
        return;
    }

    try {
        const response = await fetch(`${EXCHANGE_RATE_API_URL}/${pair}`, {method: "DELETE"});
        if (!response.ok && response.status !== 204) {
            await readResponse(response);
        }
        await loadExchangeRates();
    } catch (error) {
        setStatus(exchangeRatesStatusElement, error.message, true);
    }
}

function resetExchangeRateSearch() {
    exchangeRateSearchForm.reset();
    loadExchangeRates();
}

function setStatus(element, message, isError = false) {
    element.textContent = message;
    element.classList.toggle("error", isError);
}
