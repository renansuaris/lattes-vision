<template>
  <section class="panel bibliographic-panel" aria-labelledby="bibliographic-production-title">
    <div class="panel-body">
      <div class="section-header bibliographic-panel__header">
        <div>
          <h2 id="bibliographic-production-title" class="section-title">Produção bibliográfica</h2>
        </div>
        <strong class="bibliographic-panel__total">{{ total }} itens</strong>
      </div>

      <dl class="bibliographic-breakdown">
        <div>
          <dt>Artigos em periódicos</dt>
          <dd>{{ artigos }}</dd>
        </div>
        <div
          :class="{ 'bibliographic-breakdown__item--interactive': interactiveEventWorks }"
          :role="interactiveEventWorks ? 'button' : undefined"
          :tabindex="interactiveEventWorks ? 0 : undefined"
          @click="openEventWorks"
          @keydown.enter="openEventWorks"
        >
          <dt>Trabalhos completos em eventos</dt>
          <dd>{{ trabalhosEventos }}</dd>
        </div>
        <div
          :class="{ 'bibliographic-breakdown__item--interactive': interactiveBooks }"
          :role="interactiveBooks ? 'button' : undefined"
          :tabindex="interactiveBooks ? 0 : undefined"
          @click="openBooks"
          @keydown.enter="openBooks"
          @keydown.space.prevent="openBooks"
        >
          <dt>Livros</dt>
          <dd>{{ livros }}</dd>
        </div>
        <div
          :class="{ 'bibliographic-breakdown__item--interactive': interactiveBooks }"
          :role="interactiveBooks ? 'button' : undefined"
          :tabindex="interactiveBooks ? 0 : undefined"
          @click="openBookChapters"
          @keydown.enter="openBookChapters"
          @keydown.space.prevent="openBookChapters"
        >
          <dt>Capítulos</dt>
          <dd>{{ capitulos }}</dd>
        </div>
      </dl>
    </div>
  </section>
</template>

<script setup lang="ts">
const props = defineProps<{
  total: number
  artigos: number
  trabalhosEventos: number
  livros: number
  capitulos: number
  interactiveBooks?: boolean
  interactiveEventWorks?: boolean
}>()

const emit = defineEmits<{
  openBooks: []
  openBookChapters: []
  openEventWorks: []
}>()

function openEventWorks() {
  if (props.interactiveEventWorks) {
    emit('openEventWorks')
  }
}
function openBooks() {
  if (props.interactiveBooks) emit('openBooks')
}
function openBookChapters() {
  if (props.interactiveBooks) emit('openBookChapters')
}
</script>
