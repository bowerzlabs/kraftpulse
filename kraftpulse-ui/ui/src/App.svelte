<script lang="ts">
    import Router from 'svelte-spa-router'
    import { routes } from './routes'
    import { location } from 'svelte-spa-router'

    let mobileMenuOpen = false

    const navItems = [
        { path: '/', label: 'Overview', icon: 'M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z' },
        { path: '/requests', label: 'HTTP Requests', icon: 'M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4' },
        { path: '/queries', label: 'Queries (JPA/Mongo)', icon: 'M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4' },
        { path: '/outbound', label: 'Outbound HTTP', icon: 'M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14' },
        { path: '/exceptions', label: 'Exceptions', icon: 'M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
        { path: '/cache', label: 'Cache', icon: 'M13 10V3L4 14h7v7l9-11h-7z' },
        { path: '/lifecycle', label: 'Startup & Jobs', icon: 'M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z' }
    ]

    function toggleMobileMenu() {
        mobileMenuOpen = !mobileMenuOpen
    }

    function closeMobileMenu() {
        mobileMenuOpen = false
    }
</script>

<svelte:head>
    <title>KraftPulse — Spring Boot Observability</title>
</svelte:head>

<div class="flex flex-col lg:flex-row h-screen bg-[#0B0F17] text-gray-200 overflow-hidden font-sans">

    <!-- Sidebar -->
    <aside class="w-full lg:w-64 border-r border-gray-800/80 bg-[#0E131F] flex flex-col justify-between shrink-0">
        <!-- Header Brand -->
        <div class="p-6 border-b border-gray-800/60 flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/30 flex items-center justify-center">
                <span class="w-3 h-3 rounded-full bg-emerald-400 animate-pulse"></span>
            </div>
            <div>
                <h1 class="font-bold text-lg text-white tracking-wide">KraftPulse</h1>
                <span class="text-[10px] text-emerald-400 font-mono tracking-wider uppercase bg-emerald-950/60 px-1.5 py-0.5 rounded border border-emerald-800/40">Spring Boot</span>
            </div>
        </div>

        <!-- Single Nav Item -->
        <nav class="p-3 flex-1">
            <div class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                <span>Overview</span>
            </div>
        </nav>

        <!-- Status Footer -->
        <div class="p-4 border-t border-gray-800/60 bg-[#0B0F17]/40 text-xs text-gray-500 space-y-1">
            <div class="flex justify-between items-center">
                <span>Storage</span>
                <span class="text-gray-300 font-mono">SQLite (Local)</span>
            </div>
            <div class="flex justify-between items-center">
                <span>Status</span>
                <span class="text-emerald-400 font-medium">● Connected</span>
            </div>
        </div>
    </aside>

    <!-- Main Content Area -->
    <main class="flex-1 bg-[#0B0F17] flex items-center justify-center p-8">
        <div class="text-center space-y-3">
            <h2 class="text-3xl font-bold text-white tracking-tight">Hello World!</h2>
            <p class="text-gray-400 text-sm max-w-md">
                KraftPulse dashboard engine is running cleanly on Spring Boot.
            </p>
        </div>
    </main>

</div>