<script lang="ts">
    import type { ApplicationDetails } from '../api/kraftpulse'

    export let application: ApplicationDetails | null = null
    export let loading = false
    export let error: string | null = null

    // Runtime.totalMemory() - Runtime.freeMemory() = heap currently in use
    $: usedMb = application ? Math.max(0, application.totalMemoryMb - application.freeMemoryMb) : 0
    $: maxMb = application?.maxMemoryMb ?? 0
    $: usedPct = maxMb > 0 ? Math.min(100, (usedMb / maxMb) * 100) : 0
    $: barColor =
        usedPct > 85 ? 'bg-red-500' : usedPct > 70 ? 'bg-yellow-500' : 'bg-emerald-500'
</script>

<div class="p-5 border-b border-gray-800/60 flex flex-col gap-3 shrink-0">
    <div class="flex items-center gap-3 w-full">
        <div class="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center shrink-0">
            <span class="w-3 h-3 rounded-full bg-emerald-400 animate-pulse"></span>
        </div>

        <div class="min-w-0 flex-1">
            <h1 class="font-bold text-lg text-white tracking-wide">KraftPulse</h1>
            <div class="mt-1 flex flex-wrap items-center gap-1.5">
                <span class="text-[10px] text-emerald-400 font-mono tracking-wider uppercase bg-emerald-950/60 px-1.5 py-0.5 rounded border border-emerald-800/40">
                    Boot {application?.springBootVersion ?? '—'}
                </span>
                {#if application?.kraftPulseVersion}
                    <span class="text-[10px] text-gray-400 font-mono">v{application.kraftPulseVersion}</span>
                {/if}
            </div>
        </div>
    </div>

    <!-- Details: scrolls on short screens so the nav below stays reachable -->
    <div class="w-full space-y-2 text-xs text-gray-400 border-t border-gray-800/40 pt-3 max-h-[40vh] overflow-y-auto">
        <div class="flex justify-between items-center gap-3">
            <span class="text-gray-500">App</span>
            <span class="text-white font-mono truncate max-w-[150px]" title={application?.appName ?? ''}>
                {#if loading && !application}Loading...{:else}{application?.appName ?? '—'}{/if}
            </span>
        </div>

        {#if application?.appVersion}
            <div class="flex justify-between items-center gap-3">
                <span class="text-gray-500">Version</span>
                <span class="text-gray-200 font-mono">{application.appVersion}</span>
            </div>
        {/if}

        {#if application?.jvmVersion}
            <div class="flex justify-between items-center gap-3">
                <span class="text-gray-500">JVM</span>
                <span class="text-gray-200 font-mono text-[11px] truncate max-w-[150px]"
                      title={`${application.jvmVendor} ${application.jvmVersion}`}>
                    {application.jvmVersion}
                </span>
            </div>
        {/if}

        {#if application?.jvmVendor}
            <div class="flex justify-between items-center gap-3">
                <span class="text-gray-500">Vendor</span>
                <span class="text-gray-200 font-mono text-[11px] truncate max-w-[150px]"
                      title={application.jvmVendor}>
                    {application.jvmVendor}
                </span>
            </div>
        {/if}

        {#if application?.osName}
            <div class="flex justify-between items-center gap-3">
                <span class="text-gray-500">OS</span>
                <span class="text-gray-200 font-mono text-[11px] truncate max-w-[150px]"
                      title={`${application.osName} (${application.osArch})`}>
                    {application.osName} ({application.osArch})
                </span>
            </div>
        {/if}

        {#if application}
            <div class="pt-1 space-y-1.5">
                <div class="flex justify-between items-center gap-3">
                    <span class="text-gray-500">Heap used</span>
                    <span class="text-gray-200 font-mono text-[11px]">
                        {usedMb} / {application.maxMemoryMb} MB
                    </span>
                </div>
                <div class="h-1.5 w-full rounded-full bg-gray-800 overflow-hidden"
                     role="progressbar" aria-label="Heap usage"
                     aria-valuemin="0" aria-valuemax="100" aria-valuenow={Math.round(usedPct)}>
                    <div class={`h-full ${barColor} transition-all duration-500`} style={`width: ${usedPct}%`}></div>
                </div>
                <div class="flex justify-between text-[10px] text-gray-500 font-mono">
                    <span>Allocated {application.totalMemoryMb} MB</span>
                    <span>Free {application.freeMemoryMb} MB</span>
                </div>
            </div>
        {/if}

        {#if application?.activeProfiles?.length}
            <div class="pt-1">
                <span class="text-[10px] text-gray-500 uppercase block mb-1 tracking-wider">Active Profiles</span>
                <div class="flex flex-wrap gap-1">
                    {#each application.activeProfiles as profile}
                        <span class="text-[10px] font-mono text-gray-300 bg-gray-800/60 px-1.5 py-0.5 rounded border border-gray-700/50">
                            {profile}
                        </span>
                    {/each}
                </div>
            </div>
        {/if}

        <div class="flex justify-between items-center gap-3 pt-1">
            <span class="text-gray-500">Storage</span>
            <span class="text-gray-300 font-mono">SQLite (Local)</span>
        </div>

        <div class="flex justify-between items-center gap-3">
            <span class="text-gray-500">Status</span>
            {#if loading}
                <span class="text-gray-400 font-medium">● Connecting</span>
            {:else if application && !error}
                <span class="text-emerald-400 font-medium">● Connected</span>
            {:else if application}
                <span class="text-yellow-400 font-medium" title={error ?? ''}>● Stale</span>
            {:else}
                <span class="text-red-400 font-medium" title={error ?? ''}>● Unavailable</span>
            {/if}
        </div>

        {#if error}
            <p class="text-[10px] text-red-400">
                {application ? 'Showing last known values. Refresh failed.' : 'Could not load application details.'}
            </p>
        {/if}
    </div>
</div>