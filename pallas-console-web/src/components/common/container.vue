<template>
    <div class="my-container">
        <Navbar></Navbar>
        <router-view></router-view>
        <Version-footer></Version-footer>
    </div>
</template>

<style scoped>
.my-container {
    height: 100%;
}
</style>
