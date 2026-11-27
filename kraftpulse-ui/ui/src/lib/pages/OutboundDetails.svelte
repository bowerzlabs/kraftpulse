<script lang="ts">
    import { onMount } from 'svelte'
    import { location, pop, push } from 'svelte-spa-router'
    import {
        getOutboundDetails,
        type OutboundCallDetail
    } from '../api/kraftpulse'

    let detail: OutboundCallDetail | null = null
    let loading = true
    let errorMsg: string | null = null

    $: outboundId = decodeURIComponent($location.split('/').pop() ?? '')

    async function loadOutboundDetails(id: string) {
        if (!id) {
            errorMsg = 'Missing outbound call ID'
            loading = false
            return
        }

        loading = true
        errorMsg = null
        detail = null

        try {
            detail = await getOutboundDetails(id)
        } catch (e) {
            errorMsg = e instanceof Error ? e.message : 'Failed to load outbound call details'
        } finally {
            loading = false
        }
    }

    $: if (outboundId) {
        loadOutboundDetails(outboundId)
    }

    onMount(() => {
        if (!outboundId && !errorMsg) {
            errorMsg = 'Missing outbound call ID'
            loading = false
        }
    })

    function goBack() {
        if (window.history.length > 1) {
            pop()
        } else {
            push('/')
        }
    }

    function formatDuration(ms: number | null | undefined): string {
        if (ms == null || ms < 0) return '-'
        if (ms < 1) return `${ms.toFixed(2)} ms`
        if (ms < 1000) return `${Math.round(ms)} ms`
        return `${(ms / 1000).toFixed(2)} s`
    }

    function formatBytes(bytes: number | null | undefined): string {
        if (bytes == null || bytes < 0) return '-'
        if (bytes < 1024) return `${bytes} B`
        if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
        return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
    }

    function formatDate(value: string | null | undefined): string {
        if (!value) return '-'
        const parsed = new Date(value)
        return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString()
    }

    function statusBadgeClass(status: number, isError: boolean): string {
        if (isError || status >= 500 || status < 0) {
            return 'bg-red-950/60 text-red-400 border-red-800/60'
        }
        if (status >= 400) {
            return 'bg-yellow-950/60 text-yellow-400 border-yellow-800/60'
        }
        if (status >= 200 && status < 300) {
            return 'bg-emerald-950/60 text-emerald-400 border-emerald-800/60'
        }
        return 'bg-gray-800 text-gray-300 border-gray-700'
    }

    $: requestHeaderEntries = Object.entries(detail?.requestHeaders ?? {})
    $: responseHeaderEntries = Object.entries(detail?.responseHeaders ?? {})
</script>

{#if loading}
    <div class="flex flex-col items-center justify-center py-24 space-y-3 px-4">
        <div class="w-8 h-8 border-2 border-emerald-500/20 border-t-emerald-500 rounded-full animate-spin"></div>
        <p class="text-xs font-mono text-gray-500">Loading outbound HTTP call details...</p>
    </div>
{:else if errorMsg}
    <div class="p-4 sm:p-8 max-w-4xl mx-auto space-y-4">
        <button
            type="button"
            on:click={goBack}
            class="px-3 py-1.5 text-xs font-mono text-gray-300 hover:text-white rounded-lg border border-gray-800 bg-[#0E131F] cursor-pointer"
        >
            ← Back
        </button>
        <div class="rounded-xl border border-red-900/50 bg-red-950/20 p-5 sm:p-6 text-red-300">
            <h3 class="font-semibold text-red-400 text-base sm:text-lg">Failed to load outbound call</h3>
            <p class="mt-1 text-xs sm:text-sm text-red-300/80">{errorMsg}</p>
        </div>
    </div>
{:else if detail}
    <div class="p-4 sm:p-6 md:p-8 space-y-6 sm:space-y-8 max-w-[1600px] mx-auto w-full min-w-0">

        <!-- Top Header -->
        <header class="rounded-xl border border-gray-800/80 bg-[#0E131F]/90 p-4 sm:p-6 shadow-xl backdrop-blur flex flex-col sm:flex-row sm:items-center justify-between gap-4 sm:gap-6">
            <div class="space-y-2.5 min-w-0">
                <div class="flex flex-wrap items-center gap-2 sm:gap-3">
                    <button
                        type="button"
                        on:click={goBack}
                        class="px-2.5 py-1 text-xs font-mono text-gray-300 hover:text-white rounded border border-gray-800 bg-gray-900 cursor-pointer shrink-0"
                    >
                        ← Back
                    </button>
                    <span class="font-mono text-[11px] sm:text-xs text-gray-500 bg-gray-900 px-2.5 py-1 rounded border border-gray-800 truncate max-w-[220px] sm:max-w-none">
                        {detail.id}
                    </span>
                    <span class={`rounded-md border px-2 py-0.5 text-xs font-mono font-semibold ${statusBadgeClass(detail.statusCode, detail.error)}`}>
                        {detail.httpMethod} {detail.statusCode >= 0 ? detail.statusCode : 'ERR'}
                        {#if detail.statusText}
                            <span class="font-normal opacity-85">· {detail.statusText}</span>
                        {/if}
                    </span>
                    {#if detail.clientType}
                        <span class="rounded-md border border-pink-800/60 bg-pink-950/50 px-2 py-0.5 text-xs font-mono text-pink-300">
                            {detail.clientType}
                        </span>
                    {/if}
                </div>

                <h1 class="text-base sm:text-xl font-bold font-mono text-white tracking-tight break-all">
                    {detail.url}
                </h1>

                {#if detail.uriTemplate && detail.uriTemplate !== detail.url}
                    <p class="text-xs font-mono text-gray-400 break-all">
                        Template: <span class="text-gray-200">{detail.uriTemplate}</span>
                    </p>
                {/if}
            </div>

            <div class="sm:text-right border-t sm:border-t-0 border-gray-800/60 pt-3 sm:pt-0 shrink-0 space-y-1">
                <div class="text-2xl sm:text-3xl font-bold font-mono text-emerald-400">
                    {formatDuration(detail.durationMs)}
                </div>
                <span class="text-[10px] uppercase font-mono tracking-wider text-gray-500 block">
                    Total Duration · Connect {formatDuration(detail.connectionTimeMs)}
                </span>
            </div>
        </header>

        <!-- Error Banner (if failed or 4xx/5xx) -->
        {#if detail.error || detail.errorType || detail.errorMessage}
            <section class="rounded-xl border border-red-900/60 bg-red-950/20 p-4 sm:p-5 space-y-2 font-mono text-xs">
                <div class="flex items-center justify-between gap-2">
                    <h2 class="font-semibold text-red-400 text-sm">Outbound Call Error</h2>
                    {#if detail.errorType}
                        <span class="text-[11px] text-red-300 bg-red-950/70 border border-red-800/60 px-2 py-0.5 rounded break-all">
                            {detail.errorType}
                        </span>
                    {/if}
                </div>
                <p class="text-red-200 break-words">
                    {detail.errorMessage ?? `Request completed with status ${detail.statusCode}`}
                </p>
            </section>
        {/if}

        <!-- Timing & Payload Metrics -->
        <section class="grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-5">
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Duration</p>
                <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-emerald-400">{formatDuration(detail.durationMs)}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Connection Time</p>
                <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-cyan-400">{formatDuration(detail.connectionTimeMs)}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Request Size</p>
                <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-white">{formatBytes(detail.requestBodySize)}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4">
                <p class="text-[10px] font-mono uppercase text-gray-500">Response Size</p>
                <p class="mt-1 font-mono text-sm sm:text-base font-semibold text-white">{formatBytes(detail.responseBodySize)}</p>
            </div>
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] p-3.5 sm:p-4 col-span-2 lg:col-span-1">
                <p class="text-[10px] font-mono uppercase text-gray-500">Timestamp</p>
                <p class="mt-1 font-mono text-xs text-gray-300 truncate">{formatDate(detail.timestamp)}</p>
            </div>
        </section>

        <!-- Target & Correlation Tri-Card -->
        <section class="grid grid-cols-1 gap-4 sm:gap-6 lg:grid-cols-3">
            <!-- Target Endpoint -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Target Endpoint</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Target Service</span>
                        <span class="text-pink-400 break-all">{detail.targetService ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Host & Port</span>
                        <span class="text-gray-200 break-all">{detail.host ?? '-'}:{detail.port}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Client</span>
                        <span class="text-gray-200">{detail.clientType ?? '-'}</span>
                    </div>
                </div>
            </div>

            <!-- Trace & Request Correlation -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Correlation Context</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Parent Request ID</span>
                        {#if detail.requestId}
                            <a
                                href={`#/requests/${encodeURIComponent(detail.requestId)}`}
                                class="text-cyan-400 hover:underline break-all"
                            >
                                {detail.requestId}
                            </a>
                        {:else}
                            <span class="text-gray-500">None (background / async)</span>
                        {/if}
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Trace ID</span>
                        <span class="text-gray-200 break-all">{detail.traceId ?? '-'}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Span / Parent Span</span>
                        <span class="text-gray-300 break-all block">{detail.spanId ?? '-'}</span>
                        <span class="text-gray-500 text-[11px] break-all block">Parent: {detail.parentSpanId ?? '-'}</span>
                    </div>
                </div>
            </div>

            <!-- Application & Status -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F]">
                <div class="border-b border-gray-800/60 p-4">
                    <h2 class="font-semibold text-white text-sm">Execution Metadata</h2>
                </div>
                <div class="p-4 space-y-3 font-mono text-xs">
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Application</span>
                        <span class="text-purple-400 break-all">{detail.applicationName}</span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">HTTP Status</span>
                        <span class="text-gray-200">
                            {detail.statusCode} {detail.statusText ? `(${detail.statusText})` : ''}
                        </span>
                    </div>
                    <div>
                        <span class="text-gray-500 block text-[10px] uppercase">Initiated At (UTC)</span>
                        <span class="text-gray-400 text-[11px] break-all">{detail.timestamp}</span>
                    </div>
                </div>
            </div>
        </section>

        <!-- Request & Response Headers -->
        <section class="grid grid-cols-1 gap-4 sm:gap-6 lg:grid-cols-2">
            <!-- Request Headers -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
                <div class="border-b border-gray-800/60 p-4 sm:p-5 flex items-center justify-between">
                    <h2 class="font-semibold text-white text-sm sm:text-base">Request Headers</h2>
                    <span class="text-xs font-mono text-gray-500">{requestHeaderEntries.length}</span>
                </div>
                {#if requestHeaderEntries.length === 0}
                    <div class="p-6 text-center text-xs font-mono text-gray-500">No request headers recorded.</div>
                {:else}
                    <div class="divide-y divide-gray-800/40 font-mono text-xs">
                        {#each requestHeaderEntries as [name, value]}
                            <div class="p-3.5 sm:px-5 flex flex-col sm:flex-row sm:items-start justify-between gap-1.5 sm:gap-4">
                                <span class="text-cyan-400 shrink-0 break-all">{name}</span>
                                <span class="text-gray-300 break-all sm:text-right">{value}</span>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>

            <!-- Response Headers -->
            <div class="rounded-xl border border-gray-800/80 bg-[#0E131F] shadow-lg overflow-hidden">
                <div class="border-b border-gray-800/60 p-4 sm:p-5 flex items-center justify-between">
                    <h2 class="font-semibold text-white text-sm sm:text-base">Response Headers</h2>
                    <span class="text-xs font-mono text-gray-500">{responseHeaderEntries.length}</span>
                </div>
                {#if responseHeaderEntries.length === 0}
                    <div class="p-6 text-center text-xs font-mono text-gray-500">No response headers recorded.</div>
                {:else}
                    <div class="divide-y divide-gray-800/40 font-mono text-xs">
                        {#each responseHeaderEntries as [name, value]}
                            <div class="p-3.5 sm:px-5 flex flex-col sm:flex-row sm:items-start justify-between gap-1.5 sm:gap-4">
                                <span class="text-emerald-400 shrink-0 break-all">{name}</span>
                                <span class="text-gray-300 break-all sm:text-right">{value}</span>
                            </div>
                        {/each}
                    </div>
                {/if}
            </div>
        </section>

    </div>
{/if}