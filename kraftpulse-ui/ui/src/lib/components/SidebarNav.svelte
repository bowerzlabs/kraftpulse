<script lang="ts">
    import { location, push } from 'svelte-spa-router'

    export let onNavigate: () => void = () => {}

    const navItems = [
        { path: '/', label: 'Overview', icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z' },
        { path: '/requests', label: 'HTTP Requests', icon: 'M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4' },
        { path: '/queries', label: 'Queries (JPA/Mongo)', icon: 'M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4' },
        { path: '/outbound', label: 'Outbound HTTP', icon: 'M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14' },
        { path: '/exceptions', label: 'Exceptions', icon: 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
        { path: '/cache', label: 'Cache & Metrics', icon: 'M13 10V3L4 14h7v7l9-11h-7z' },
        { path: '/lifecycle', label: 'Startup & Jobs', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' }
    ]

    function navigate(path: string) {
        push(path)
        onNavigate()
    }
</script>

<nav class="p-3 flex-1 space-y-1 overflow-y-auto">
    {#each navItems as item}
        {@const isActive = $location === item.path || ($location.startsWith(item.path) && item.path !== '/')}
        <button
            on:click={() => navigate(item.path)}
            class={`w-full flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                isActive
                    ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    : 'text-gray-400 hover:bg-gray-800/40 hover:text-gray-200'
            }`}
        >
            <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d={item.icon}/>
            </svg>
            <span>{item.label}</span>
        </button>
    {/each}
</nav>