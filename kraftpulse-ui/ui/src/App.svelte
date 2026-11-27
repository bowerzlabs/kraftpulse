<script lang="ts">
    import { onMount } from 'svelte'
    import Router from 'svelte-spa-router'
    import { routes } from './routes'
    import { getAppDetails, type ApplicationDetails } from './lib/api/kraftpulse'
    import Sidebar from './lib/components/Sidebar.svelte'

    const POLL_INTERVAL_MS = 5000

    let mobileMenuOpen = false
    let application: ApplicationDetails | null = null
    let applicationLoading = true
    let applicationError: string | null = null

    let timer: ReturnType<typeof setInterval> | undefined
    let inFlight = false

    async function loadApplication() {
        if (inFlight) return // never stack requests when the server is slow
        inFlight = true
        try {
            application = await getAppDetails()
            applicationError = null
        } catch (e) {
            // keep the last good data so the sidebar doesn't blank out
            applicationError = e instanceof Error ? e.message : 'Failed to load application details'
        } finally {
            applicationLoading = false
            inFlight = false
        }
    }

    function stopPolling() {
        if (timer !== undefined) clearInterval(timer)
        timer = undefined
    }

    function startPolling() {
        stopPolling()
        timer = setInterval(loadApplication, POLL_INTERVAL_MS)
    }

    function handleVisibility() {
        if (document.visibilityState === 'visible') {
            loadApplication()
            startPolling()
        } else {
            stopPolling() // no polling while the tab is hidden
        }
    }

    // Not async: onMount has to return the cleanup function synchronously
    onMount(() => {
        loadApplication()
        startPolling()
        document.addEventListener('visibilitychange', handleVisibility)
        return () => {
            stopPolling()
            document.removeEventListener('visibilitychange', handleVisibility)
        }
    })
</script>

<svelte:head>
    <title>KraftPulse — Spring Boot Observability</title>
</svelte:head>

<div class="flex flex-col lg:flex-row h-screen bg-[#0B0F17] text-gray-200 overflow-hidden font-sans">

    <!-- Mobile header -->
    <header class="lg:hidden flex items-center justify-between border-b border-gray-800/80 bg-[#0E131F] px-4 py-3 shrink-0">
        <div class="flex items-center gap-3 min-w-0">
            <div class="w-7 h-7 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center shrink-0">
                <span class="w-2.5 h-2.5 rounded-full bg-emerald-400 animate-pulse"></span>
            </div>
            <p class="text-sm text-white font-semibold truncate">
                {application?.appName ?? 'KraftPulse'}
            </p>
        </div>

        <button
            on:click={() => (mobileMenuOpen = !mobileMenuOpen)}
            class="p-2 rounded-lg text-gray-400 hover:text-white hover:bg-gray-800/60 focus:outline-none"
            aria-label="Toggle navigation menu"
        >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                {#if mobileMenuOpen}
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                {:else}
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                {/if}
            </svg>
        </button>
    </header>

    <Sidebar
        {application}
        loading={applicationLoading}
        error={applicationError}
        open={mobileMenuOpen}
        onClose={() => (mobileMenuOpen = false)}
    />

    <!-- Main content -->
    <main class="flex-1 bg-[#0B0F17] overflow-y-auto min-w-0">
        <Router {routes} />
    </main>
</div>