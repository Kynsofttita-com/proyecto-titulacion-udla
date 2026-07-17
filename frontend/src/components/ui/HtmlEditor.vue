<template>
  <div class="html-editor border border-ink-300 rounded-lg overflow-hidden bg-white">
    <!-- Toolbar -->
    <div v-if="editor" class="border-b border-ink-200 bg-ink-50 px-2 py-1.5 flex flex-wrap items-center gap-1">
      <!-- Grupo: Formato basico -->
      <button
        type="button"
        @click="editor.chain().focus().toggleBold().run()"
        :class="btnClass(editor.isActive('bold'))"
        title="Negrita (Ctrl+B)"
      >
        <i class="pi pi-bold text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().toggleItalic().run()"
        :class="btnClass(editor.isActive('italic'))"
        title="Cursiva (Ctrl+I)"
      >
        <i class="pi pi-italic text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().toggleUnderline().run()"
        :class="btnClass(editor.isActive('underline'))"
        title="Subrayado (Ctrl+U)"
      >
        <span class="text-xs font-medium underline">U</span>
      </button>

      <span class="mx-1 h-5 w-px bg-ink-300"></span>

      <!-- Grupo: Encabezados -->
      <button
        type="button"
        @click="editor.chain().focus().toggleHeading({ level: 1 }).run()"
        :class="btnClass(editor.isActive('heading', { level: 1 }))"
        title="Titulo 1"
      >
        <span class="text-xs font-bold">H1</span>
      </button>
      <button
        type="button"
        @click="editor.chain().focus().toggleHeading({ level: 2 }).run()"
        :class="btnClass(editor.isActive('heading', { level: 2 }))"
        title="Titulo 2"
      >
        <span class="text-xs font-bold">H2</span>
      </button>
      <button
        type="button"
        @click="editor.chain().focus().toggleHeading({ level: 3 }).run()"
        :class="btnClass(editor.isActive('heading', { level: 3 }))"
        title="Titulo 3"
      >
        <span class="text-xs font-bold">H3</span>
      </button>

      <span class="mx-1 h-5 w-px bg-ink-300"></span>

      <!-- Grupo: Listas -->
      <button
        type="button"
        @click="editor.chain().focus().toggleBulletList().run()"
        :class="btnClass(editor.isActive('bulletList'))"
        title="Lista con vinetas"
      >
        <i class="pi pi-list text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().toggleOrderedList().run()"
        :class="btnClass(editor.isActive('orderedList'))"
        title="Lista numerada"
      >
        <i class="pi pi-sort-numeric-down text-xs" />
      </button>

      <span class="mx-1 h-5 w-px bg-ink-300"></span>

      <!-- Grupo: Alineacion -->
      <button
        type="button"
        @click="editor.chain().focus().setTextAlign('left').run()"
        :class="btnClass(editor.isActive({ textAlign: 'left' }))"
        title="Alinear a la izquierda"
      >
        <i class="pi pi-align-left text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().setTextAlign('center').run()"
        :class="btnClass(editor.isActive({ textAlign: 'center' }))"
        title="Centrar"
      >
        <i class="pi pi-align-center text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().setTextAlign('right').run()"
        :class="btnClass(editor.isActive({ textAlign: 'right' }))"
        title="Alinear a la derecha"
      >
        <i class="pi pi-align-right text-xs" />
      </button>

      <span class="mx-1 h-5 w-px bg-ink-300"></span>

      <!-- Enlace -->
      <button
        type="button"
        @click="insertarEnlace"
        :class="btnClass(editor.isActive('link'))"
        title="Insertar enlace"
      >
        <i class="pi pi-link text-xs" />
      </button>

      <!-- Deshacer / Rehacer -->
      <span class="mx-1 h-5 w-px bg-ink-300"></span>
      <button
        type="button"
        @click="editor.chain().focus().undo().run()"
        :disabled="!editor.can().undo()"
        :class="btnClass(false)"
        title="Deshacer (Ctrl+Z)"
      >
        <i class="pi pi-undo text-xs" />
      </button>
      <button
        type="button"
        @click="editor.chain().focus().redo().run()"
        :disabled="!editor.can().redo()"
        :class="btnClass(false)"
        title="Rehacer (Ctrl+Y)"
      >
        <i class="pi pi-refresh text-xs" />
      </button>

      <!-- Boton Insertar variable (a la derecha) -->
      <div class="ml-auto relative" v-if="variables && variables.length > 0">
        <button
          type="button"
          @click="mostrarVariables = !mostrarVariables"
          class="px-2.5 py-1 rounded bg-brand-50 text-brand-700 hover:bg-brand-100 flex items-center gap-1 text-xs font-medium border border-brand-200 transition-colors"
        >
          <i class="pi pi-plus-circle text-xs" />
          Insertar variable
          <i class="pi pi-chevron-down text-[10px]" />
        </button>
        <div
          v-if="mostrarVariables"
          class="absolute right-0 top-full mt-1 bg-white border border-ink-200 rounded-lg shadow-lg z-10 min-w-[240px] max-h-64 overflow-y-auto"
        >
          <div class="px-3 py-2 text-xs font-semibold text-ink-500 border-b border-ink-200">
            Variables disponibles
          </div>
          <button
            v-for="v in variables"
            :key="v.key"
            type="button"
            @click="insertarVariable(v.key)"
            class="w-full text-left px-3 py-2 hover:bg-brand-50 text-xs border-b border-ink-100 last:border-b-0"
          >
            <div class="font-mono text-brand-700">{{ formatVar(v.key) }}</div>
            <div class="text-ink-500 text-[11px] mt-0.5" v-if="v.descripcion">{{ v.descripcion }}</div>
          </button>
        </div>
      </div>
    </div>

    <!-- Editor content -->
    <editor-content :editor="editor" class="tiptap-content" />
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, watch, ref } from 'vue'
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import Underline from '@tiptap/extension-underline'
import TextAlign from '@tiptap/extension-text-align'

interface Variable {
  key: string
  descripcion?: string
}

const props = defineProps<{
  modelValue: string
  variables?: Variable[]
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const mostrarVariables = ref(false)

const editor = useEditor({
  content: props.modelValue || '',
  extensions: [
    StarterKit,
    Underline,
    Link.configure({
      openOnClick: false,
      HTMLAttributes: { class: 'text-brand-600 underline' }
    }),
    TextAlign.configure({
      types: ['heading', 'paragraph']
    })
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  }
})

// Sincronizar cambios externos del modelValue (p.ej. al cargar plantilla existente)
watch(() => props.modelValue, (nuevo) => {
  const actual = editor.value?.getHTML()
  if (editor.value && nuevo !== actual) {
    editor.value.commands.setContent(nuevo || '', false)
  }
})

// Cerrar dropdown al hacer clic fuera
function handleClickOutside(e: MouseEvent) {
  const target = e.target as HTMLElement
  if (!target.closest('.html-editor')) {
    mostrarVariables.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  editor.value?.destroy()
})

function btnClass(active: boolean): string {
  return [
    'inline-flex items-center justify-center w-7 h-7 rounded text-ink-700 hover:bg-ink-200 transition-colors',
    active ? 'bg-brand-100 text-brand-700' : '',
    'disabled:opacity-40 disabled:cursor-not-allowed'
  ].filter(Boolean).join(' ')
}

function insertarEnlace() {
  const url = window.prompt('URL del enlace:')
  if (url === null) return
  if (url === '') {
    editor.value?.chain().focus().extendMarkRange('link').unsetLink().run()
    return
  }
  editor.value?.chain().focus().extendMarkRange('link').setLink({ href: url }).run()
}

function insertarVariable(key: string) {
  editor.value?.chain().focus().insertContent(`{{${key}}}`).run()
  mostrarVariables.value = false
}

function formatVar(key: string): string {
  return '{{' + key + '}}'
}
</script>

<style>
.tiptap-content .ProseMirror {
  min-height: 220px;
  padding: 12px 16px;
  outline: none;
  font-size: 14px;
  color: #1f2937;
  line-height: 1.6;
}
.tiptap-content .ProseMirror p {
  margin: 0 0 8px 0;
}
.tiptap-content .ProseMirror h1 {
  font-size: 22px;
  font-weight: 700;
  margin: 8px 0;
}
.tiptap-content .ProseMirror h2 {
  font-size: 18px;
  font-weight: 700;
  margin: 8px 0;
}
.tiptap-content .ProseMirror h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 8px 0;
}
.tiptap-content .ProseMirror ul {
  list-style: disc;
  padding-left: 24px;
  margin: 6px 0;
}
.tiptap-content .ProseMirror ol {
  list-style: decimal;
  padding-left: 24px;
  margin: 6px 0;
}
.tiptap-content .ProseMirror a {
  color: #2563eb;
  text-decoration: underline;
}
.tiptap-content .ProseMirror u {
  text-decoration: underline;
}
</style>
