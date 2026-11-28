// upload-utils.js
// Utilidades para subir archivos a Cloudflare R2 de forma segura

const WORKER_URL = "https://nebula-upload.cristiandeniz669.workers.dev";
const PUBLIC_R2_URL = "https://pub-d0efcbc0b2ae4c67a2a251a43f2c2e67.r2.dev";
const MAX_SIZE = 15 * 1024 * 1024; // 15 MB

const ALLOWED_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
  "image/avif",
  "image/gif"
];

const EXT_MAP = {
  "image/jpeg": "jpg",
  "image/png": "png",
  "image/webp": "webp",
  "image/avif": "avif",
  "image/gif": "gif"
};

// Función para sanitizar nombres de archivos (cumple la regex del Worker)
function sanitizeName(name) {
  return name
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-zA-Z0-9_-]/g, "_")
    .replace(/_+/g, "_")
    .replace(/^_+|_+$/g, "");
}

// Sube un archivo a R2 usando el token de Firebase
// Retorna una promesa que resuelve con la URL pública
async function uploadToR2(file, keyPath, onProgress) {
  return new Promise(async (resolve, reject) => {
    try {
      // 1. Validaciones locales
      if (!ALLOWED_TYPES.includes(file.type)) {
        return reject(new Error(`Formato no permitido: ${file.type}`));
      }
      if (file.size > MAX_SIZE) {
        return reject(new Error(`El archivo supera los 15 MB`));
      }

      // 2. Obtener el token de Firebase del usuario actual
      const auth = window.firebaseAuth;
      if (!auth || !auth.currentUser) {
        return reject(new Error("No hay sesión de administrador activa"));
      }
      
      const idToken = await auth.currentUser.getIdToken();

      // 3. Preparar la petición
      const url = `${WORKER_URL}/upload?key=${encodeURIComponent(keyPath)}`;
      
      const xhr = new XMLHttpRequest();
      xhr.open("PUT", url, true);
      xhr.setRequestHeader("Authorization", `Bearer ${idToken}`);
      xhr.setRequestHeader("Content-Type", file.type);

      xhr.upload.onprogress = (event) => {
        if (event.lengthComputable && onProgress) {
          onProgress(Math.round((event.loaded / event.total) * 100));
        }
      };

      xhr.onload = () => {
        try {
          const res = JSON.parse(xhr.responseText);
          if (xhr.status >= 200 && xhr.status < 300 && res.success) {
            resolve(res.url);
          } else {
            reject(new Error(res.error || `Error del servidor (${xhr.status})`));
          }
        } catch (e) {
          reject(new Error(`Respuesta inválida del servidor`));
        }
      };

      xhr.onerror = () => reject(new Error("Error de red al subir la imagen"));
      xhr.onabort = () => reject(new Error("Subida cancelada"));

      xhr.send(file);
    } catch (error) {
      reject(error);
    }
  });
}

// Función para generar la ruta de una portada
function getCoverPath(workId, ext) {
  return `covers/${workId}/cover.${ext}`;
}

// Función para generar la ruta de un banner
function getBannerPath(workId, ext) {
  return `banners/${workId}/banner.${ext}`;
}

// Función para generar la ruta de una página de capítulo
function getPagePath(workId, chapterId, pageNumber, ext) {
  const num = String(pageNumber).padStart(3, "0");
  return `comics/${workId}/${chapterId}/${num}.${ext}`;
}

// Elimina un archivo de R2
async function deleteFromR2(keyPath) {
  const auth = window.firebaseAuth;
  if (!auth || !auth.currentUser) throw new Error("No hay sesión activa");
  
  const idToken = await auth.currentUser.getIdToken();
  const url = `${WORKER_URL}/delete?key=${encodeURIComponent(keyPath)}`;
  
  const res = await fetch(url, {
    method: "DELETE",
    headers: { "Authorization": `Bearer ${idToken}` }
  });
  
  const data = await res.json();
  if (!res.ok) throw new Error(data.error || "Error eliminando archivo");
  return data;
}

export { 
  uploadToR2, 
  getCoverPath, 
  getBannerPath, 
  getPagePath, 
  deleteFromR2,
  sanitizeName,
  EXT_MAP,
  PUBLIC_R2_URL
};