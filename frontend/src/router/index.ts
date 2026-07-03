import { createRouter, createWebHistory } from 'vue-router'

import HomeView from '../views/HomeView.vue'
import GrupoAdhocView from '../views/GrupoAdhocView.vue'
import PesquisadorView from '../views/PesquisadorView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/pesquisadores/:id',
      name: 'pesquisador',
      component: PesquisadorView,
      props: true,
    },
    {
      path: '/grupos/adhoc',
      name: 'grupo-adhoc',
      component: GrupoAdhocView,
    },
  ],
})

export default router
