<template>
  <section class="panel bibliographic-panel" aria-labelledby="bibliographic-production-title">
    <div class="panel-body">
      <div class="section-header bibliographic-panel__header">
        <div>
          <h2 id="bibliographic-production-title" class="section-title">Produção bibliográfica</h2>
          <p class="section-subtitle">Itens registrados nos currículos analisados.</p>
        </div>
        <strong class="bibliographic-panel__total">{{ total }} itens</strong>
      </div>

      <dl class="bibliographic-breakdown">
        <div>
          <dt>Artigos em periódicos</dt>
          <dd>{{ artigos }}</dd>
        </div>
        <div>
          <dt>Trabalhos completos em eventos</dt>
          <dd>{{ trabalhosEventos }}</dd>
        </div>
        <div>
          <dt>Livros</dt>
          <dd>{{ livros }}</dd>
        </div>
        <div>
          <dt>Capítulos</dt>
          <dd>{{ capitulos }}</dd>
        </div>
      </dl>
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  total: number
  artigos: number
  trabalhosEventos: number
  livros: number
  capitulos: number
}>()
</script>
