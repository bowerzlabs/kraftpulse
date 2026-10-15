<script lang="ts">
    import { onMount } from 'svelte'
    import {
        getRequestDetails,
        type RequestDetailsResponse,
        type WaterfallNode,
        type SpanType
    } from '../api/kraftpulse'
    import { location } from 'svelte-spa-router'

    let request: RequestDetailsResponse | null = null
    let loading = true
    let error: string | null = null

    $: traceId =$location.split('/').pop() ?? ''

    onMount(async () => {
        try {
            request = await getRequestDetails(traceId)
        } catch (e) {
            error = e instanceof Error ? e.message : 'Failed to load request trace details'
        } finally {
            loading = false
        }
    })

    function formatDuration(ms: number | null | undefined): string {
        if (ms == null) return '-'
        if (ms < 1) return `${ms.toFixed(2)} ms`
        if (ms < 1000) return `${Math.round(ms)} ms`
        return `${(ms / 1000).toFixed(2)} s`
    }

    function formatDate(value: string | null): string {
        if (!value) return '-'
        return new Date(value).toLocaleString()
    }

    function statusBadgeClass(status: number | null): string {
        if (status == null) return 'bg-gray-800 text-gray-400 border-gray-700'
        if (status >= 500) return 'bg-red-950/60 text-red-400 border-red-800/60'
        if (status >= 400) return 'bg-yellow-950/60 text-yellow-400 border-yellow-800/60'
        if (status >= 200 && status < 300) return 'bg-emerald-950/60 text-emerald-400 border-emerald-800/60'
        return 'bg-gray-800 text-gray-300 border-gray-700'
    }

    function spanColorClass(type: SpanType): string {
        switch (type) {
            case 'HTTP': return 'text-blue-400'
            case 'CONTROLLER': return 'text-purple-400'
            case 'SERVICE': return 'text-cyan-400'
            case 'DATABASE': case 'REPOSITORY': return 'text-orange-400'
            case 'OUTBOUND': return 'text-pink-400'
            case 'CACHE': return 'text-emerald-400'
            case 'SECURITY': return 'text-red-400'
            default: return 'text-gray-400'
        }
    }

    function waterfallWidth(node: WaterfallNode): number {
        if (!request?.performance.totalDurationMs) return 0
        return Math.max(2, (node.durationMs / request.performance.totalDurationMs) * 100)
    }

    function waterfallOffset(node: WaterfallNode): number {
        if (!request?.performance.totalDurationMs) return 0
        return (node.startOffsetMs / request.performance.totalDurationMs) * 100
    }
</script>

{#if loading}
    <div class="flex flex-col items-center justify-center py-24 space-y-3 px-4">
        <div class="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
        <p class="text-xs font-mono text-gray-500">Retrieving trace timeline...</p>
    </div>
{:else if error}
    <div class="p-4 sm:p-8 max-w-4xl mx-auto">
        <div class="rounded-xl border border-red-900/50 bg-red-950/20 p-5 sm:p-6 text-red-300">
            <h3 class="font-semibold text-red-400 text-base sm:text-lg">Failed to load request trace</h3>
            <p class="mt-1 text-xs sm:text-sm text-red-300/80">{error}</p>
        </div>
    </div>
{:else if request}
    <div class="p-4 sm:p-6 md:p-8 space-y-6 sm:space-y-8 max-w-[1600px] mx-auto w-full min-w-0">

        <!-- Top Header & Meta Bar -->
        <header class="rounded-xl border border-gray-800/80 bg-[#0E131F]/90 p-4 sm:p-6 shadow-xl backdrop-blur flex flex-col sm:flex-row sm:items-center justify-between gap-4 sm:gap-6">
            <div class="space-y-2 min-w-0">
                <div class="flex flex-wrap items-center gap-2 sm:gap-3">
                    <span class="font-mono text-[11px] sm:text-xs text-gray-500 bg-gray-900 px-2 sm:px-2.5 py-1 rounded border border-gray-800 truncate max-w-[200px] sm:max-w-none">
                        {request.traceId}
                    </span>
                    {#if request.statusCode}
                        <span class={`rounded-md border px-2 py-0.5 text-xs font-mono font-semibold ${statusBadgeClass(request.statusCode)}`}>
                            {request.method} {request.statusCode}
                        </span>
                    {/if}
                    {#if request.performance.slow}
                        <span class="rounded-md border border-yellow-800/60 bg-yellow-950/60 px-2 py-0.5 text-xs text-yellow-400 font-mono">
                            Slow Execution
                        </span>
                    {/if}
                </div>
                <h1 class="text-lg sm:text-xl font-bold font-mono text-white tracking-tight break-all">{request.path ?? '/'}</h1>
            </div>

            <div class="sm:text-right border-t sm:border-t-0 border-gray-800/60 pt-3 sm:pt-0 shrink-0">
                <div class="text-2xl sm:text-3xl font-bold font-mono text-emerald-400">
                    {formatDuration(request.durationMs)}
                </div>
                <span class="text-[10px] uppercase font-mono tracking-wider text-gray-500">Total Duration</span>
            </div>
        </header>

        <!-- HTTP Overview Grid -->
        <section class="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-4">
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Method</p>
                <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-white truncate">{request.method ?? '-'}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4 min-w-0">
                <p class="text-[10px] font-mono uppercase text-gray-500">Path</p>
                <p class="mt-1 font-mono text-xs sm:text-sm text-gray-200 truncate">{request.path ?? '-'}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4 min-w-0">
                <p class="text-[10px] font-mono uppercase text-gray-500">Route</p>
                <p class="mt-1 font-mono text-xs sm:text-sm text-gray-200 truncate">{request.route ?? '-'}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Started</p>
                <p class="mt-1 font-mono text-xs text-gray-300 truncate">{formatDate(request.startedAt)}</p>
            </div>
        </section>

        <!-- Waterfall Timeline -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="border-b border-gray-800/60 p-4 sm:p-5">
                <h2 class="font-semibold text-white text-sm sm:text-base">Request Waterfall</h2>
                <p class="text-xs text-gray-500 mt-0.5">Execution timeline breakdown per internal span</p>
            </div>

            <div class="p-4 sm:p-6">
                {#if request.waterfall.length === 0}
                    <div class="py-8 text-center text-xs font-mono text-gray-500">No execution spans recorded for this trace.</div>
                {:else}
                    <div class="space-y-4 sm:space-y-3">
                        {#each request.waterfall as node}
                            <div class="flex flex-col sm:flex-row sm:items-center gap-2 sm:gap-4 border-b sm:border-0 border-gray-800/40 pb-3 sm:pb-0">
                                <div class="flex items-center justify-between sm:justify-start gap-2 shrink-0">
                                    <span class="w-24 text-[11px] sm:text-xs font-mono font-semibold {spanColorClass(node.type)}">
                                        {node.type}
                                    </span>
                                    <span class="sm:hidden font-mono text-xs text-gray-400">
                                        {formatDuration(node.durationMs)}
                                    </span>
                                </div>
                                <div class="w-full sm:w-56 md:w-64 shrink-0 truncate font-mono text-xs text-gray-300" title={node.name}>
                                    {node.name}
                                </div>
                                <div class="relative h-4 sm:h-6 flex-1 w-full overflow-hidden rounded bg-[#0B0F17] border border-gray-800/40 my-1 sm:my-0">
                                    <div
                                        class="absolute top-0 h-full rounded bg-emerald-500/30 border border-emerald-500/50"
                                        style={`left: ${waterfallOffset(node)}%; width: ${waterfallWidth(node)}%`}
                                    ></div>
                                </div>
                                <div class="hidden sm:block w-24 shrink-0 text-right font-mono text-xs text-gray-400">
                                    {formatDuration(node.durationMs)}
                                </div>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </section>

        <!-- Performance Breakdown Grid -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="border-b border-gray-800/60 p-4 sm:p-5">
                <h2 class="font-semibold text-white text-sm sm:text-base">Performance Breakdown</h2>
            </div>

            <div class="grid grid-cols-2 gap-px bg-gray-800/60 md:grid-cols-5">
                <div class="bg-[#0E131F] p-3.5 sm:p-4">
                    <span class="text-[10px] font-mono text-gray-500 uppercase">Total</span>
                    <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-white">{formatDuration(request.performance.totalDurationMs)}</p>
                </div>
                <div class="bg-[#0E131F] p-3.5 sm:p-4">
                    <span class="text-[10px] font-mono text-gray-500 uppercase">Database</span>
                    <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-orange-400">{formatDuration(request.performance.databaseDurationMs)}</p>
                </div>
                <div class="bg-[#0E131F] p-3.5 sm:p-4">
                    <span class="text-[10px] font-mono text-gray-500 uppercase">Outbound</span>
                    <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-pink-400">{formatDuration(request.performance.outboundDurationMs)}</p>
                </div>
                <div class="bg-[#0E131F] p-3.5 sm:p-4">
                    <span class="text-[10px] font-mono text-gray-500 uppercase">Cache</span>
                    <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-emerald-400">{formatDuration(request.performance.cacheDurationMs)}</p>
                </div>
                <div class="bg-[#0E131F] p-3.5 sm:p-4 col-span-2 md:col-span-1">
                    <span class="text-[10px] font-mono text-gray-500 uppercase">Critical Path</span>
                    <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-cyan-400">{formatDuration(request.performance.criticalPathDurationMs)}</p>
                </div>
            </div>

            {#if request.performance.criticalPath.length > 0}
                <div class="border-t border-gray-800/60 p-4 sm:p-5 bg-[#0B0F17]/40">
                    <p class="text-[10px] font-mono uppercase tracking-wider text-gray-500 mb-3">Critical Path Flow</p>
                    <div class="flex flex-wrap items-center gap-2">
                        {#each request.performance.criticalPath as item, index}
                            <span class="rounded-md bg-gray-900 border border-gray-800 px-2.5 sm:px-3 py-1 font-mono text-[11px] sm:text-xs text-gray-300">
                                {item}
                            </span>
                            {#if index < request.performance.criticalPath.length - 1}
                                <span class="text-gray-600 font-mono text-xs">→</span>
                            {/if}
                        {/each}
                    </div>
                </div>
            {/if}
        </section>

        <!-- Controller, Security, and Client Tri-Card -->
        <section class="grid grid-cols-1 gap-4 sm:gap-6 lg:grid-cols-3">
            <!-- Controller -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Controller Context</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Class</span>
                        <span class="text-purple-400 break-all">{request.controller?.className ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Method</span>
                        <span class="text-gray-200">{request.controller?.methodName ?? '-'}</span>
                    </div>
                </div>
            </div>

            <!-- Security -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Security Context</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div class="flex justify-between items-center">
                        <span class="text-gray-500 text-[10px] uppercase">Authenticated</span>
                        <span class={request.security?.authenticated ? 'text-emerald-400 font-bold' : 'text-gray-400'}>
                            {request.security?.authenticated ? 'Yes' : 'No'}
                        </span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Principal</span>
                        <span class="text-gray-200 break-all">{request.security?.principal ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Authentication Type</span>
                        <span class="text-gray-200">{request.security?.authenticationType ?? '-'}</span>
                    </div>
                </div>
            </div>

            <!-- Client -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Client Information</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">IP Address</span>
                        <span class="text-gray-200">{request.client?.ip ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Host</span>
                        <span class="text-gray-200 truncate block">{request.client?.host ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">User Agent</span>
                        <span class="text-gray-400 text-[11px] break-all">{request.client?.userAgent ?? '-'}</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Database Queries Section -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-gray-800/60 p-4 sm:p-5 gap-3">
                <div>
                    <h2 class="font-semibold text-white text-sm sm:text-base">Database Telemetry</h2>
                    <p class="text-xs text-gray-500 mt-0.5">
                        {request.database.queryCount} total queries · {formatDuration(request.database.totalDurationMs)} execution time
                    </p>
                </div>
                {#if request.database.slowQueryCount > 0}
                    <span class="self-start sm:self-auto px-2.5 py-1 text-xs font-mono text-yellow-400 bg-yellow-950/60 border border-yellow-800/60 rounded-md">
                        {request.database.slowQueryCount} Slow Query
                    </span>
                {/if}
            </div>

            {#if request.database.queries.length === 0}
                <div class="p-8 text-center text-xs font-mono text-gray-500">No database queries executed during this request.</div>
            {:else}
                <div class="divide-y divide-gray-800/40">
                    {#each request.database.queries as query}
                        <div class="p-4 sm:p-5 space-y-3">
                            <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                                <div class="flex items-center gap-2">
                                    {#if query.slow}
                                        <span class="px-2 py-0.5 text-[10px] font-mono text-yellow-400 bg-yellow-950/60 border border-yellow-800/60 rounded shrink-0">
                                            SLOW
                                        </span>
                                    {/if}
                                    <span class="font-mono text-xs text-emerald-400 font-semibold truncate">
                                        {query.repositoryMethod ?? 'Query'}
                                    </span>
                                </div>
                                <span class="font-mono text-xs text-gray-400 font-semibold self-end sm:self-auto">
                                    {formatDuration(query.durationMs)}
                                </span>
                            </div>
                            <pre class="overflow-x-auto rounded-lg bg-[#0B0F17] p-3 sm:p-4 font-mono text-[11px] sm:text-xs text-gray-300 border border-gray-800/60">{query.query}</pre>
                        </div>
                    {/each}
                </div>
            {/if}
        </section>

        <!-- Outbound External Calls -->
        <section class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
            <div class="border-b border-gray-800/60 p-4 sm:p-5">
                <h2 class="font-semibold text-white text-sm sm:text-base">Outbound HTTP Requests</h2>
                <p class="text-xs text-gray-500 mt-0.5">{request.outboundCalls.length} external calls recorded</p>
            </div>

            {#if request.outboundCalls.length === 0}
                <div class="p-8 text-center text-xs font-mono text-gray-500">No outbound calls made.</div>
            {:else}
                <div class="divide-y divide-gray-800/40">
                    {#each request.outboundCalls as call}
                        <div class="flex flex-col sm:flex-row sm:items-center justify-between p-4 font-mono text-xs gap-2 sm:gap-4">
                            <div class="flex items-center gap-2.5 min-w-0">
                                <span class="px-2 py-0.5 bg-gray-800 text-gray-300 rounded text-[10px] shrink-0">{call.method}</span>
                                <span class="text-gray-200 truncate">{call.url}</span>
                            </div>
                            <div class="flex items-center justify-between sm:justify-end gap-3 shrink-0 pt-1 sm:pt-0 border-t sm:border-0 border-gray-800/40">
                                {#if call.statusCode}
                                    <span class={`px-2 py-0.5 rounded text-[10px] sm:text-[11px] border ${statusBadgeClass(call.statusCode)}`}>
                                        {call.statusCode}
                                    </span>
                                {/if}
                                <span class="text-gray-400 font-semibold">{formatDuration(call.durationMs)}</span>
                            </div>
                        </div>
                    {/each}
                </div>
            {/if}
        </section>

        <!-- Captured Exception Block -->
        {#if request.error}
            <section class="rounded-xl border border-red-900/60 bg-red-950/20 shadow-lg overflow-hidden">
                <div class="border-b border-red-900/40 p-4 sm:p-5 flex items-center justify-between">
                    <h2 class="font-semibold text-red-400 text-sm sm:text-base">Unhandled Exception</h2>
                    <span class="px-2.5 py-0.5 text-xs font-mono text-red-400 bg-red-950/80 border border-red-800/60 rounded-full">
                        {request.error.occurrenceCount} Occurrences
                    </span>
                </div>
                <div class="p-4 sm:p-6 space-y-4 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Exception Type</span>
                        <span class="text-red-400 font-semibold text-xs sm:text-sm break-all">{request.error.type ?? 'Unknown Exception'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Message</span>
                        <span class="text-gray-200 text-xs sm:text-sm break-words">{request.error.message ?? 'No detail message.'}</span>
                    </div>
                    {#if request.error.stacktrace}
                        <details class="group">
                            <summary class="cursor-pointer text-gray-400 hover:text-white transition-colors text-xs font-sans">
                                Toggle Full Stacktrace
                            </summary>
                            <pre class="mt-3 max-h-96 overflow-auto rounded-lg bg-[#0B0F17] p-3 sm:p-4 text-[10px] sm:text-[11px] text-gray-400 border border-gray-800/60 leading-relaxed">{request.error.stacktrace}</pre>
                        </details>
                    {/if}
                </div>
            </section>
        {/if}

    </div>
{/if}