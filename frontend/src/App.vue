<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <RouterLink class="brand" to="/">
          <img class="brand-logo" :src="logoUrl" alt="LattesVision" />
        </RouterLink>
        <nav class="header-nav" aria-label="Navegacao principal">
          <a href="#" @click.prevent>Inicio</a>
          <a href="#" @click.prevent>Sobre</a>
        </nav>
      </div>
    </header>

    <RouterView />

    <footer class="app-footer">
      <div class="app-footer__inner">LattesVision</div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import logoUrl from './assets/Lattes_VISION__1_-removebg-preview.png'
</script>
