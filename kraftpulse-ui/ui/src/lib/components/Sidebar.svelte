<script lang="ts">
    import type { ApplicationDetails } from '../api/kraftpulse'
    import AppBrand from './AppBrand.svelte'
    import SidebarNav from './SidebarNav.svelte'

    export let application: ApplicationDetails | null = null
    export let loading = false
    export let error: string | null = null
    export let open = false
    export let onClose: () => void = () => {}
</script>

{#if open}
    <!-- svelte-ignore a11y_click_events_have_key_events, a11y_no_static_element_interactions -->
    <div
        class="fixed inset-0 z-40 bg-black/70 backdrop-blur-sm lg:hidden"
        on:click={onClose}
    ></div>
{/if}

<aside class={`
    fixed lg:static inset-y-0 left-0 z-50 w-72 lg:w-64
    border-r border-gray-800/80 bg-[#0E131F] flex flex-col shrink-0
    transform transition-transform duration-300 ease-in-out
    ${open ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
`}>

    <!-- Mobile drawer header -->
    <div class="lg:hidden p-4 border-b border-gray-800/60 flex items-center justify-between shrink-0">
        <span class="text-xs font-mono text-emerald-400 uppercase tracking-wider">Navigation Menu</span>
        <button on:click={onClose} class="text-gray-400 hover:text-white text-sm font-mono">
            ✕ Close
        </button>
    </div>


    <!-- Navigation links -->
    <SidebarNav onNavigate={onClose} />
</aside>