<script lang="ts">
    import { onMount } from 'svelte'
    import { getOverview, type OverviewResponse } from '../api/kraftpulse'

    let overview: OverviewResponse | null = null
    let loading = true
    let error: string | null = null

    onMount(async () => {
        try {
            overview = await getOverview()
        } catch (e) {
            console.error("Dashboard fetch exception: ", e)
            error = e instanceof Error ? e.message : 'Failed to load telemetry overview'
        } finally {
            loading = false
        }
    })
</script>

<div class="p-8 space-y-8 max-w-[1600px] mx-auto w-full">

    <!-- Top Server Status Bar (Pulse Inspired) -->
    <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]/90 p-4 shadow-xl backdrop-blur flex flex-wrap items-center justify-between gap-6">
        <div class="flex items-center gap-4">
            <div class="flex items-center gap-2">
                <span class="relative flex h-3 w-3">
                  <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
                  <span class="relative inline-flex rounded-full h-3 w-3 bg-emerald-500"></span>
                </span>
                <span class="font-bold text-white text-base">app-name</span>
            </div>
            <span class="text-xs font-mono text-gray-400 bg-gray-800/60 px-2.5 py-1 rounded-md">app profile</span>
        </div>

        <div class="flex items-center gap-8 text-xs font-mono">
            <div>
                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Requests (1h)</span>
                <span class="text-white text-sm font-semibold">{overview?.totalRequestsLastHour ?? 0}</span>
            </div>
            <div class="h-8 w-[1px] bg-gray-800"></div>
            <div>
                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Avg Latency</span>
                <span class="text-white text-sm font-semibold">{overview?.averageResponseTimeMs?.toFixed(1) ?? 0} ms</span>
            </div>
            <div class="h-8 w-[1px] bg-gray-800"></div>
            <div>
                <span class="text-gray-500 block text-[10px] uppercase tracking-wider">Error Rate</span>
                <span class={`text-sm font-semibold ${overview && overview.errorRatePercent > 0 ? 'text-red-400' : 'text-emerald-400'}`}>
                    {overview?.errorRatePercent?.toFixed(2) ?? '0.00'}%
                </span>
            </div>
        </div>
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

        <!-- Pulse Grid: Slowest Endpoints & Exceptions -->
        <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">

            <!-- Card 1: Slowest Endpoints -->
            <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg flex flex-col justify-between">
                <div class="border-b border-gray-800/60 p-5 flex items-center justify-between">
                    <div>
                        <h2 class="font-semibold text-white text-base flex items-center gap-2">
                            <svg class="w-4 h-4 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                            Slowest Endpoints
                        </h2>
                        <p class="text-xs text-gray-500 mt-0.5">Highest average latency HTTP controllers</p>
                    </div>
                    <span class="text-[11px] text-gray-500 font-mono">100ms threshold</span>
                </div>

                <div class="divide-y divide-gray-800/40">
                    {#each overview.slowestEndpoints as endpoint}
                        <div class="flex items-center justify-between p-4 hover:bg-gray-800/20 transition-colors">
                            <span class="font-mono text-xs text-emerald-400 bg-emerald-950/40 border border-emerald-800/30 px-2 py-1 rounded truncate max-w-md">
                                {endpoint.path}
                            </span>
                            <span class="font-mono text-xs text-gray-300 font-semibold shrink-0">
                                {endpoint.averageDurationMs.toFixed(1)} ms
                            </span>
                        </div>
                    {:else}
                        <div class="p-6 text-center text-xs text-gray-500 font-mono">No slow endpoints recorded.</div>
                    {/each}
                </div>
            </section>

            <!-- Card 2: Recent Exceptions -->
            <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg flex flex-col justify-between">
                <div class="border-b border-gray-800/60 p-5 flex items-center justify-between">
                    <div>
                        <h2 class="font-semibold text-white text-base flex items-center gap-2">
                            <svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                            Recent Exceptions
                        </h2>
                        <p class="text-xs text-gray-500 mt-0.5">Grouped application errors</p>
                    </div>
                    <span class="px-2 py-0.5 text-xs font-mono text-red-400 bg-red-950/60 border border-red-800/40 rounded-full">
                        {overview.recentExceptionCount} Total
                    </span>
                </div>

                <div class="divide-y divide-gray-800/40">
                    {#each overview.recentExceptions as ex}
                        <div class="p-4 hover:bg-gray-800/20 transition-colors space-y-1">
                            <div class="flex items-center justify-between">
                                <span class="font-mono text-xs text-red-400 font-semibold truncate">{ex.exceptionType}</span>
                                <span class="text-[10px] text-gray-500 font-mono">{ex.timestamp}</span>
                            </div>
                            <p class="text-xs text-gray-300 truncate">{ex.message}</p>
                            <span class="text-[10px] text-gray-500 font-mono block">{ex.originClass}</span>
                        </div>
                    {:else}
                        <div class="p-6 text-center text-xs text-gray-500 font-mono">No exceptions captured. Application running cleanly!</div>
                    {/each}
                </div>
            </section>

        </div>

        <!-- Stream: Recent Requests Table -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg">
            <div class="border-b border-gray-800/60 p-5 flex items-center justify-between">
                <div>
                    <h2 class="font-semibold text-white text-base">Recent Request Stream</h2>
                    <p class="text-xs text-gray-500 mt-0.5">Live trace records captured by KraftPulse</p>
                </div>
            </div>

            <div class="divide-y divide-gray-800/40">
                {#each overview.recentRequests as req}
                    <a
                        href={`#/requests/${encodeURIComponent(req.traceId)}`}
                        class="flex items-center justify-between p-4 hover:bg-gray-800/30 transition-colors group"
                    >
                        <div class="flex items-center gap-4 min-w-0">
                            <span class={`px-2 py-1 text-[11px] font-mono font-bold rounded border ${
                                req.statusCode >= 500 ? 'bg-red-950/60 border-red-800/60 text-red-400' :
                                req.statusCode >= 400 ? 'bg-yellow-950/60 border-yellow-800/60 text-yellow-400' :
                                'bg-emerald-950/60 border-emerald-800/60 text-emerald-400'
                            }`}>
                                {req.method} {req.statusCode}
                            </span>
                            <span class="font-mono text-xs text-gray-200 truncate group-hover:text-emerald-400 transition-colors">
                                {req.path}
                            </span>
                        </div>

                        <div class="flex items-center gap-6 shrink-0 font-mono text-xs text-gray-400">
                            <span class="text-gray-500 text-[11px]">{req.traceId.slice(0, 8)}...</span>
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