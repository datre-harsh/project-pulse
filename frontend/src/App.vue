<template>
  <v-app>
    <v-app-bar color="primary" density="comfortable">
      <v-app-bar-title>Project Pulse</v-app-bar-title>
    </v-app-bar>

    <v-navigation-drawer permanent width="260">
      <v-list nav>
        <v-list-item to="/" title="Dashboard" prepend-icon="mdi-view-dashboard"></v-list-item>
        <v-list-item to="/sections" title="Sections" prepend-icon="mdi-school"></v-list-item>
        <v-list-item to="/teams" title="Teams" prepend-icon="mdi-account-multiple"></v-list-item>
        <v-list-item to="/rubric" title="Rubric" prepend-icon="mdi-clipboard-text"></v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="py-6">
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>
