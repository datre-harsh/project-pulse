<template>
  <v-app>
    <v-app-bar color="primary" density="comfortable">
      <v-app-bar-title>Project Pulse</v-app-bar-title>
    </v-app-bar>

    <v-navigation-drawer permanent width="260">
      <v-list nav density="comfortable">
        <v-list-item to="/" title="Home" prepend-icon="mdi-view-dashboard"></v-list-item>

        <v-list-subheader>Setup</v-list-subheader>
        <v-list-item to="/rubric" title="Rubric" prepend-icon="mdi-clipboard-text"></v-list-item>
        <v-list-item to="/sections" title="Sections" prepend-icon="mdi-school"></v-list-item>
        <v-list-item to="/students" title="Students" prepend-icon="mdi-account-search"></v-list-item>
        <v-list-item to="/instructors" title="Instructors" prepend-icon="mdi-account-tie"></v-list-item>
        <v-list-item to="/teams" title="Teams" prepend-icon="mdi-account-multiple"></v-list-item>

        <v-list-subheader>Student Work</v-list-subheader>
        <v-list-item to="/war" title="Weekly Activities" prepend-icon="mdi-chart-line"></v-list-item>
        <v-list-item to="/peer-evaluation" title="Peer Evaluation" prepend-icon="mdi-account-group"></v-list-item>
        <v-list-item to="/peer-evaluation-report" title="My Report" prepend-icon="mdi-chart-box"></v-list-item>

        <v-list-subheader>Instructor Reports</v-list-subheader>
        <v-list-item to="/evaluate-student" title="Section Evaluations" prepend-icon="mdi-account-check"></v-list-item>
        <v-list-item to="/team-war-report" title="Team WAR" prepend-icon="mdi-file-chart"></v-list-item>
        <v-list-item to="/student-peer-eval-report" title="Student History" prepend-icon="mdi-account-search"></v-list-item>

        <v-divider class="my-2"></v-divider>
        <v-list-item to="/profile" title="Profile" prepend-icon="mdi-account-circle"></v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-main>
      <v-container class="py-6">
        <router-view />
      </v-container>
    </v-main>
  </v-app>
</template>
