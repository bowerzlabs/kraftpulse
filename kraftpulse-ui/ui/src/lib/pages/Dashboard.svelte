<script lang="ts">
    import { onMount } from 'svelte'
    import { getOverview, getAppDetails, type OverviewResponse, type ApplicationDetails } from '../api/kraftpulse'
    import AppBrand from '../components/AppBrand.svelte'

    let overview: OverviewResponse | null = null
    let application: ApplicationDetails | null = null
    let loading = true
    let error: string | null = null

    onMount(async () => {
        try {
            [overview, application] = await Promise.all([
                getOverview(),
                getAppDetails()
            ])
        } catch (e) {
            console.error("Dashboard fetch exception: ", e)
            error = e instanceof Error ? e.message : 'Failed to load telemetry overview'
        } finally {
            loading = false
        }
    })

    function formatDate(value: string | null | undefined): string {
        if (!value) return '-'
        return new Date(value).toLocaleTimeString()
    }
</script>

<div class="p-4 sm:p-6 md:p-8 space-y-6 sm:space-y-8 max-w-[1600px] mx-auto w-full">

    <!-- Top Server Status Bar using AppBrand -->
    <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]/90 shadow-xl backdrop-blur overflow-hidden">
            <AppBrand {application} loading={loading} error={error} />

           {#if overview}
                        <div class="flex items-center gap-4 sm:gap-8 text-xs font-mono flex-wrap px-6 pb-4 lg:pb-0">
                            <div>
                                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Requests (1h)</span>
                                <span class="text-white text-sm font-semibold">{overview.totalRequestsLastHour}</span>
                            </div>
                            <div class="h-8 w-[1px] bg-gray-800 hidden sm:block"></div>
                            <div>
                                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Avg Latency</span>
                                <span class="text-white text-sm font-semibold">{overview.averageResponseTimeMs.toFixed(1)} ms</span>
                            </div>
                            <div class="h-8 w-[1px] bg-gray-800 hidden sm:block"></div>
                            <div>
                                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Error Rate</span>
                                <span class={`text-sm font-semibold ${overview.errorRatePercent > 0 ? 'text-red-400' : 'text-emerald-400'}`}>
                                    {overview.errorRatePercent.toFixed(2)}%
                                </span>
                            </div>
                            <div class="h-8 w-[1px] bg-gray-800 hidden sm:block"></div>
                            <div>
                                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Heap Free</span>
                                <span class="text-emerald-400 text-sm font-semibold">{application?.freeMemoryMb ?? 0} MB</span>
                            </div>
                        </div>
                    {/if}
    </div>

    {#if loading}
        <div class="flex flex-col items-center justify-center py-24 space-y-3">
            <div class="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
            <p class="text-xs font-mono text-gray-500">Retrieving application telemetry...</p>
        </div>
    {:else if error}
        <div class="rounded-xl border border-red-900/50 bg-red-950/20 p-6 text-red-300">
            <h3 class="font-semibold text-red-400 text-lg">Failed to sync with KraftPulse</h3>
            <p class="mt-1 text-sm text-red-300/80">{error}</p>
        </div>
    {:else if overview}

        <!-- Top Metrics Cards -->
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-5 shadow-lg">
                <p class="text-xs font-medium text-gray-400">Total Throughput (24h)</p>
                <p class="mt-2 text-3xl font-bold text-white font-mono">{overview.totalRequestsLast24h}</p>
                <span class="mt-2 inline-block text-[11px] text-gray-500">Recorded across active sessions</span>
            </div>

            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-5 shadow-lg">
                <p class="text-xs font-medium text-gray-400">Avg Response Time</p>
                <p class="mt-2 text-3xl font-bold text-emerald-400 font-mono">{overview.averageResponseTimeMs.toFixed(1)} <span class="text-sm font-normal text-gray-400">ms</span></p>
                <span class="mt-2 inline-block text-[11px] text-emerald-500/80">Healthy performance window</span>
            </div>

            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-5 shadow-lg">
                <p class="text-xs font-medium text-gray-400">Exceptions Captured</p>
                <p class={`mt-2 text-3xl font-bold font-mono ${overview.recentExceptionCount > 0 ? 'text-red-400' : 'text-white'}`}>
                    {overview.recentExceptionCount}
                </p>
                <span class="mt-2 inline-block text-[11px] text-gray-500">In the last 24 hours</span>
            </div>

            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-5 shadow-lg">
                <p class="text-xs font-medium text-gray-400">Error Rate</p>
                <p class={`mt-2 text-3xl font-bold font-mono ${overview.errorRatePercent > 0 ? 'text-yellow-400' : 'text-emerald-400'}`}>
                    {overview.errorRatePercent.toFixed(2)}%
                </p>
                <span class="mt-2 inline-block text-[11px] text-gray-500">Targeting &lt; 0.50%</span>
            </div>
        </div>

        <!-- Recent Exceptions Section -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="border-b border-gray-800/60 p-5 flex items-center justify-between">
                <div>
                    <h2 class="font-semibold text-white text-base flex items-center gap-2">
                        <svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                        Recent Exceptions
                    </h2>
                    <p class="text-xs text-gray-500 mt-0.5">Grouped application errors captured by KraftPulse</p>
                </div>
                <span class="px-2.5 py-1 text-xs font-mono text-red-400 bg-red-950/60 border border-red-800/40 rounded-full">
                    {overview.recentExceptionCount} Total
                </span>
            </div>

            <div class="divide-y divide-gray-800/40">
                {#each overview.recentExceptions as ex}
                    <div class="p-4 sm:p-5 hover:bg-gray-800/20 transition-colors space-y-1">
                        <div class="flex items-center justify-between gap-2">
                            <span class="font-mono text-xs text-red-400 font-semibold truncate">{ex.exceptionType}</span>
                            <span class="text-[10px] text-gray-500 font-mono shrink-0">{formatDate(ex.lastSeen)}</span>
                        </div>
                        <p class="text-xs text-gray-300 break-words">{ex.message}</p>
                        <div class="flex items-center gap-3 pt-1 text-[10px] text-gray-500 font-mono flex-wrap">
                            <span>Origin: {ex.originClass}#{ex.originMethod}:{ex.lineNumber}</span>
                            {#if ex.requestUri}
                                <span>• Route: {ex.httpMethod} {ex.requestUri}</span>
                            {/if}
                            <span>• Occurrences: {ex.occurrenceCount}</span>
                        </div>
                    </div>
                {:else}
                    <div class="p-8 text-center text-xs text-gray-500 font-mono">No exceptions captured. Application running cleanly!</div>
                {/each}
            </div>
        </section>

        <!-- Stream: Recent Requests Table -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="border-b border-gray-800/60 p-5 flex items-center justify-between">
                <div>
                    <h2 class="font-semibold text-white text-base">Recent Request Stream</h2>
                    <p class="text-xs text-gray-500 mt-0.5">Live trace records captured by KraftPulse</p>
                </div>
            </div>

            <div class="divide-y divide-gray-800/40">
                {#each overview.recentRequests as req}
                    {@const status = req.statusCode ?? 200}
                    <a
                        href={`#/requests/${encodeURIComponent(req.eventId)}`}
                        class="flex items-center justify-between p-4 hover:bg-gray-800/30 transition-colors group"
                    >
                        <div class="flex items-center gap-4 min-w-0">
                            <span class={`px-2 py-1 text-[11px] font-mono font-bold rounded border ${
                                status >= 500 ? 'bg-red-950/60 border-red-800/60 text-red-400' :
                                status >= 400 ? 'bg-yellow-950/60 border-yellow-800/60 text-yellow-400' :
                                'bg-emerald-950/60 border-emerald-800/60 text-emerald-400'
                            }`}>
                                {req.method} {status}
                            </span>
                            <span class="font-mono text-xs text-gray-200 truncate group-hover:text-emerald-400 transition-colors">
                                {req.path}
                            </span>
                        </div>

                        <div class="flex items-center gap-6 shrink-0 font-mono text-xs text-gray-400">
                            <span class="text-gray-500 text-[11px] hidden sm:inline">{req.traceId.slice(0, 8)}...</span>
                            <span class="text-white font-medium">{req.durationMs} ms</span>
                            <span class="text-gray-600 group-hover:translate-x-1 transition-transform">→</span>
                        </div>
                    </a>
                {:else}
                    <div class="p-8 text-center text-xs text-gray-500 font-mono">No recent requests recorded yet. Make some HTTP calls to see traces populate!</div>
                {/each}
            </div>
        </section>

    {/if}

</div>