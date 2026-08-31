<template>
  <div class="app-shell">
    <header class="app-header">
      <div class="app-header__inner">
        <RouterLink class="brand" to="/">
          <img class="brand-logo" :src="logoUrl" alt="LattesVision" />
        </RouterLink>
        <nav class="header-nav" aria-label="Navegação principal">
          <RouterLink to="/">Consulta</RouterLink>
          <a href="#app-footer">Sobre</a>
        </nav>
      </div>
    </header>

    <RouterView />

    <footer id="app-footer" class="app-footer">
      <div class="app-footer__inner">Copyright © LattesVision</div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import logoUrl from './assets/logo-lattesvision-atualizada.png'
</script>
