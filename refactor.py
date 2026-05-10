import os
import shutil
import re

# Base paths
BASE_DIR = r"C:\Users\dmoha\OneDrive\Desktop\PIjava\Agrimans"
OLD_SRC_DIR = os.path.join(BASE_DIR, "Gestion parcelle", "src", "main", "java", "org", "example", "parcelle_culture")
OLD_RES_DIR = os.path.join(BASE_DIR, "Gestion parcelle", "src", "main", "resources")

NEW_JAVA_DIR = os.path.join(BASE_DIR, "src", "main", "java", "modules", "parcelle")
NEW_RES_FXML_DIR = os.path.join(BASE_DIR, "src", "main", "resources", "fxml", "parcelle")
NEW_RES_CSS_DIR = os.path.join(BASE_DIR, "src", "main", "resources", "css")

# Create directories
os.makedirs(os.path.join(NEW_JAVA_DIR, "models"), exist_ok=True)
os.makedirs(os.path.join(NEW_JAVA_DIR, "controllers"), exist_ok=True)
os.makedirs(os.path.join(NEW_JAVA_DIR, "services"), exist_ok=True)
os.makedirs(NEW_RES_FXML_DIR, exist_ok=True)
os.makedirs(NEW_RES_CSS_DIR, exist_ok=True)

# 1. Move and update Models
models_src = os.path.join(OLD_SRC_DIR, "entities")
if os.path.exists(models_src):
    for filename in os.listdir(models_src):
        if filename.endswith(".java"):
            with open(os.path.join(models_src, filename), "r", encoding="utf-8") as f:
                content = f.read()
            content = content.replace("package org.example.parcelle_culture.entities;", "package modules.parcelle.models;")
            with open(os.path.join(NEW_JAVA_DIR, "models", filename), "w", encoding="utf-8") as f:
                f.write(content)

# 2. Move and update Services
services_src = os.path.join(OLD_SRC_DIR, "services")
if os.path.exists(services_src):
    for filename in os.listdir(services_src):
        if filename.endswith(".java"):
            with open(os.path.join(services_src, filename), "r", encoding="utf-8") as f:
                content = f.read()
            content = content.replace("package org.example.parcelle_culture.services;", "package modules.parcelle.services;")
            content = content.replace("import org.example.parcelle_culture.entities", "import modules.parcelle.models")
            content = content.replace("import org.example.parcelle_culture.utils.DBConnection;", "import core.database.DBConnection;")
            with open(os.path.join(NEW_JAVA_DIR, "services", filename), "w", encoding="utf-8") as f:
                f.write(content)

# 3. Move and update Controllers
controllers_src = os.path.join(OLD_SRC_DIR, "contoller") # Yes, 'contoller' spelled wrong in user's dir
if os.path.exists(controllers_src):
    for filename in os.listdir(controllers_src):
        if filename.endswith(".java"):
            with open(os.path.join(controllers_src, filename), "r", encoding="utf-8") as f:
                content = f.read()
            content = content.replace("package org.example.parcelle_culture.contoller;", "package modules.parcelle.controllers;")
            content = content.replace("import org.example.parcelle_culture.entities", "import modules.parcelle.models")
            content = content.replace("import org.example.parcelle_culture.services", "import modules.parcelle.services")
            
            # Fix FXML resource loading paths
            content = re.sub(r'FXMLLoader\.load\(getClass\(\)\.getResource\("\/([^\"]+)\.fxml"\)\)', r'FXMLLoader.load(getClass().getResource("/fxml/parcelle/\1.fxml"))', content)
            content = re.sub(r'new FXMLLoader\(getClass\(\)\.getResource\("\/([^\"]+)\.fxml"\)\)', r'new FXMLLoader(getClass().getResource("/fxml/parcelle/\1.fxml"))', content)
            content = re.sub(r'FXMLLoader\.load\(getClass\(\)\.getResource\("([^\"]+)\.fxml"\)\)', r'FXMLLoader.load(getClass().getResource("/fxml/parcelle/\1.fxml"))', content)
            content = re.sub(r'new FXMLLoader\(getClass\(\)\.getResource\("([^\"]+)\.fxml"\)\)', r'new FXMLLoader(getClass().getResource("/fxml/parcelle/\1.fxml"))', content)
            
            with open(os.path.join(NEW_JAVA_DIR, "controllers", filename), "w", encoding="utf-8") as f:
                f.write(content)

# 4. Move FXMLs and CSS
if os.path.exists(OLD_RES_DIR):
    for filename in os.listdir(OLD_RES_DIR):
        if filename.endswith(".fxml"):
            with open(os.path.join(OLD_RES_DIR, filename), "r", encoding="utf-8") as f:
                content = f.read()
            content = content.replace('fx:controller="org.example.parcelle_culture.contoller.', 'fx:controller="modules.parcelle.controllers.')
            content = content.replace('stylesheets="@styleeee.css"', 'stylesheets="@/css/styleeee.css"')
            content = content.replace('stylesheets="/styleeee.css"', 'stylesheets="@/css/styleeee.css"')
            with open(os.path.join(NEW_RES_FXML_DIR, filename), "w", encoding="utf-8") as f:
                f.write(content)
        elif filename.endswith(".css"):
            shutil.copy(os.path.join(OLD_RES_DIR, filename), os.path.join(NEW_RES_CSS_DIR, filename))

print("Refactoring complete.")
