import Dashboard from './lib/pages/Dashboard.svelte'
import RequestDetails from './lib/pages/RequestDetails.svelte'
import NotFound from './lib/pages/NotFound.svelte'

export const routes = {
    "/": Dashboard,

    "/requests/:traceId": RequestDetails,

    "*": NotFound
}