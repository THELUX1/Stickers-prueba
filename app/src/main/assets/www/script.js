const $ = id => document.getElementById(id);
const canvas = $('stickerCanvas'), ctx = canvas.getContext('2d', {willReadFrequently:true});
const W=512,H=512;
let sourceImage=null, baseCanvas=document.createElement('canvas'), baseCtx=baseCanvas.getContext('2d');
let pristineCanvas=document.createElement('canvas'), pristineCtx=pristineCanvas.getContext('2d');
baseCanvas.width=pristineCanvas.width=W;baseCanvas.height=pristineCanvas.height=H;
let scale=1,rotation=0,outline=0,shadow=0,text='',textColor='#ffffff',font='Arial',textSize=48,textOutline=3,textY=440;
let brush=28,mode='erase',drawing=false,lastPoint=null,history=[],stickers=[];
let packName='Mi paquete de stickers',packAuthor='Creado por mí';
let toastTimer;

function toast(message){const el=$('toast');el.textContent=message;el.classList.add('show');clearTimeout(toastTimer);toastTimer=setTimeout(()=>el.classList.remove('show'),2200)}
function showView(id){document.querySelectorAll('.view').forEach(v=>v.classList.toggle('active',v.id===id));document.querySelectorAll('.nav-item').forEach(b=>b.classList.toggle('active',b.dataset.view===id));window.scrollTo({top:0,behavior:'smooth'})}
document.querySelectorAll('.nav-item').forEach(b=>b.addEventListener('click',()=>showView(b.dataset.view)));
function render(){
 ctx.clearRect(0,0,W,H);
 if(sourceImage){
  ctx.save();ctx.translate(W/2,H/2);ctx.rotate(rotation*Math.PI/180);ctx.scale(scale,scale);
  const ratio=Math.min(W/sourceImage.width,H/sourceImage.height)*.78;
  const dw=sourceImage.width*ratio,dh=sourceImage.height*ratio;
  if(shadow>0){ctx.shadowColor='rgba(0,0,0,.55)';ctx.shadowBlur=shadow;ctx.shadowOffsetY=shadow*.35}
  if(outline>0){
   const mask=document.createElement('canvas');mask.width=W;mask.height=H;const m=mask.getContext('2d');
   m.translate(W/2,H/2);m.rotate(rotation*Math.PI/180);m.scale(scale,scale);m.drawImage(baseCanvas,-dw/2,-dh/2,dw,dh);
   ctx.shadowColor='transparent';ctx.shadowBlur=0;ctx.shadowOffsetY=0;
   for(let a=0;a<Math.PI*2;a+=Math.PI/8){ctx.drawImage(mask,Math.cos(a)*outline,Math.sin(a)*outline)}
  }
  ctx.drawImage(baseCanvas,-dw/2,-dh/2,dw,dh);ctx.restore();
 }
 if(text){
  ctx.save();ctx.font=`800 ${textSize}px "${font}"`;ctx.textAlign='center';ctx.textBaseline='middle';ctx.lineJoin='round';
  if(textOutline>0){ctx.strokeStyle='#17131e';ctx.lineWidth=textOutline*2;ctx.strokeText(text,W/2,textY)}
  ctx.fillStyle=textColor;ctx.fillText(text,W/2,textY);ctx.restore();
 }
 $('emptyState').classList.toggle('hidden',!!sourceImage);
 $('saveStickerBtn').disabled=!sourceImage;
}
function snapshot(){if(!sourceImage)return;history.push(baseCtx.getImageData(0,0,W,H));if(history.length>15)history.shift()}
function loadImage(file){
 if(!file||!file.type.startsWith('image/')){toast('Selecciona una imagen válida');return}
 const url=URL.createObjectURL(file),img=new Image();
 img.onload=()=>{sourceImage=img;scale=1;rotation=0;outline=0;shadow=0;history=[];baseCtx.clearRect(0,0,W,H);pristineCtx.clearRect(0,0,W,H);
 const ratio=Math.min(W/img.width,H/img.height)*.78;baseCtx.drawImage(img,(W-img.width*ratio)/2,(H-img.height*ratio)/2,img.width*ratio,img.height*ratio);pristineCtx.drawImage(img,(W-img.width*ratio)/2,(H-img.height*ratio)/2,img.width*ratio,img.height*ratio);
 $('scaleRange').value=100;$('rotateRange').value=0;$('outlineRange').value=0;$('shadowRange').value=0;updateOutputs();render();URL.revokeObjectURL(url);toast('Imagen cargada')};
 img.onerror=()=>toast('No se pudo abrir esa imagen');img.src=url;
}
function updateOutputs(){
 $('scaleValue').textContent=Math.round(scale*100)+'%';$('rotateValue').textContent=rotation+'°';$('outlineValue').textContent=outline+' px';$('shadowValue').textContent=shadow;
 $('textSizeValue').textContent=textSize+' px';$('textOutlineValue').textContent=textOutline+' px';$('brushValue').textContent=brush+' px';
}
$('uploadBtn').onclick=$('emptyUploadBtn').onclick=()=>$('imageInput').click();
$('imageInput').addEventListener('change',e=>{loadImage(e.target.files[0]);e.target.value=''});
$('scaleRange').oninput=e=>{scale=+e.target.value/100;updateOutputs();render()};
$('rotateRange').oninput=e=>{rotation=+e.target.value;updateOutputs();render()};
$('outlineRange').oninput=e=>{outline=+e.target.value;updateOutputs();render()};
$('shadowRange').oninput=e=>{shadow=+e.target.value;updateOutputs();render()};
$('fitBtn').onclick=()=>{scale=1;rotation=0;$('scaleRange').value=100;$('rotateRange').value=0;updateOutputs();render()};
$('resetImageBtn').onclick=()=>{if(!sourceImage)return;scale=1;rotation=0;outline=0;shadow=0;history=[];baseCtx.clearRect(0,0,W,H);const ratio=Math.min(W/sourceImage.width,H/sourceImage.height)*.78;baseCtx.drawImage(sourceImage,(W-sourceImage.width*ratio)/2,(H-sourceImage.height*ratio)/2,sourceImage.width*ratio,sourceImage.height*ratio);pristineCtx.clearRect(0,0,W,H);pristineCtx.drawImage(sourceImage,(W-sourceImage.width*ratio)/2,(H-sourceImage.height*ratio)/2,sourceImage.width*ratio,sourceImage.height*ratio);$('scaleRange').value=100;$('rotateRange').value=0;$('outlineRange').value=0;$('shadowRange').value=0;updateOutputs();render()};
$('stickerText').oninput=e=>{text=e.target.value;render()};$('fontSelect').onchange=e=>{font=e.target.value;render()};$('textColor').oninput=e=>{textColor=e.target.value;render()};
$('textSize').oninput=e=>{textSize=+e.target.value;updateOutputs();render()};$('textOutline').oninput=e=>{textOutline=+e.target.value;updateOutputs();render()};
$('textTopBtn').onclick=()=>{textY=70;render()};$('textBottomBtn').onclick=()=>{textY=440;render()};
$('brushRange').oninput=e=>{brush=+e.target.value;updateOutputs()};
$('eraseModeBtn').onclick=()=>setMode('erase');$('restoreModeBtn').onclick=()=>setMode('restore');
function setMode(m){mode=m;$('eraseModeBtn').classList.toggle('selected',m==='erase');$('restoreModeBtn').classList.toggle('selected',m==='restore')}
function pointFromEvent(e){const r=canvas.getBoundingClientRect();return{x:(e.clientX-r.left)/r.width*W,y:(e.clientY-r.top)/r.height*H}}
function paintSegment(a,b){baseCtx.save();baseCtx.lineCap='round';baseCtx.lineJoin='round';baseCtx.lineWidth=brush;
 baseCtx.beginPath();baseCtx.moveTo(a.x,a.y);baseCtx.lineTo(b.x,b.y);
 if(mode==='erase'){baseCtx.globalCompositeOperation='destination-out';baseCtx.strokeStyle='rgba(0,0,0,1)';baseCtx.stroke()}
 else{baseCtx.lineWidth=brush;baseCtx.clip();baseCtx.globalCompositeOperation='source-over';baseCtx.drawImage(pristineCanvas,0,0)}
 baseCtx.restore();render()}
canvas.addEventListener('pointerdown',e=>{if(!sourceImage||!$('cutPanel').classList.contains('active'))return;drawing=true;canvas.setPointerCapture(e.pointerId);snapshot();lastPoint=pointFromEvent(e);paintSegment(lastPoint,lastPoint)});
canvas.addEventListener('pointermove',e=>{if(!drawing)return;const p=pointFromEvent(e);paintSegment(lastPoint,p);lastPoint=p});
canvas.addEventListener('pointerup',()=>{drawing=false;lastPoint=null});canvas.addEventListener('pointercancel',()=>{drawing=false;lastPoint=null});
$('undoBtn').onclick=()=>{const previous=history.pop();if(!previous){toast('No hay cambios para deshacer');return}baseCtx.putImageData(previous,0,0);render()};
$('clearCutBtn').onclick=()=>{if(!sourceImage)return;if(!confirm('¿Restaurar la imagen original?'))return;const ratio=Math.min(W/sourceImage.width,H/sourceImage.height)*.78;baseCtx.clearRect(0,0,W,H);pristineCtx.clearRect(0,0,W,H);baseCtx.drawImage(sourceImage,(W-sourceImage.width*ratio)/2,(H-sourceImage.height*ratio)/2,sourceImage.width*ratio,sourceImage.height*ratio);pristineCtx.drawImage(sourceImage,(W-sourceImage.width*ratio)/2,(H-sourceImage.height*ratio)/2,sourceImage.width*ratio,sourceImage.height*ratio);history=[];render()};
document.querySelectorAll('.tool-tab').forEach(b=>b.onclick=()=>{document.querySelectorAll('.tool-tab').forEach(x=>x.classList.toggle('active',x===b));document.querySelectorAll('.tool-panel').forEach(x=>x.classList.toggle('active',x.id===b.dataset.panel))});

async function makeBlob(){
 const qualities=[0.90,0.82,0.74,0.66,0.58,0.50];
 for(const q of qualities){
  const blob=await new Promise(resolve=>canvas.toBlob(resolve,'image/webp',q));
  if(blob && blob.size<=100*1024) return blob;
 }
 return new Promise(resolve=>canvas.toBlob(resolve,'image/webp',0.42));
}
async function saveSticker(){
 if(!sourceImage)return;if(stickers.length>=30){toast('El paquete ya tiene 30 stickers');return}
 const blob=await makeBlob();if(!blob){toast('No se pudo crear el sticker');return}
 const data=await blobToDataURL(blob);stickers.push({id:Date.now()+Math.random(),data});persist();renderPack();toast('Sticker guardado en tu colección')
}
function blobToDataURL(blob){return new Promise((resolve,reject)=>{const r=new FileReader();r.onload=()=>resolve(r.result);r.onerror=reject;r.readAsDataURL(blob)})}
$('saveStickerBtn').onclick=saveSticker;
function persist(){try{localStorage.setItem('stickerStudioData',JSON.stringify({stickers,packName,packAuthor}))}catch(e){toast('Almacenamiento lleno. Exporta tus stickers para no perderlos.')}}
function loadSaved(){try{const d=JSON.parse(localStorage.getItem('stickerStudioData')||'{}');stickers=Array.isArray(d.stickers)?d.stickers:[];packName=d.packName||packName;packAuthor=d.packAuthor||packAuthor}catch(e){stickers=[]}}
function renderPack(){
 $('stickerCount').textContent=stickers.length+' / 30';$('packNamePreview').textContent=packName;$('packAuthorPreview').textContent=packAuthor;
 $('emptyPack').style.display=stickers.length?'none':'flex';$('exportAllBtn').disabled=!stickers.length;$('addWhatsAppBtn').disabled=stickers.length<3||stickers.length>30;
 const grid=$('stickerGrid');grid.innerHTML='';
 stickers.forEach((s,i)=>{const tile=document.createElement('div');tile.className='sticker-tile';const img=document.createElement('img');img.src=s.data;img.alt='Sticker '+(i+1);const del=document.createElement('button');del.className='delete-sticker';del.textContent='×';del.setAttribute('aria-label','Eliminar sticker');del.onclick=()=>{if(confirm('¿Eliminar este sticker?')){stickers.splice(i,1);persist();renderPack()}};tile.append(img,del);grid.append(tile)});
}
$('newProjectBtn').onclick=()=>{if(sourceImage&&!confirm('¿Empezar un proyecto nuevo? Los stickers guardados no se eliminarán.'))return;sourceImage=null;text='';$('stickerText').value='';baseCtx.clearRect(0,0,W,H);render()};
$('createFirstBtn').onclick=()=>showView('editorView');
$('editPackBtn').onclick=()=>{$('packName').value=packName;$('packAuthor').value=packAuthor;$('packDialog').showModal()};
$('packForm').addEventListener('submit',e=>{if(e.submitter?.value==='save'){packName=$('packName').value.trim()||'Mi paquete';packAuthor=$('packAuthor').value.trim()||'Creado por mí';persist();renderPack();toast('Paquete actualizado')}});
async function downloadSticker(sticker,index){const a=document.createElement('a');a.href=sticker.data;a.download=`sticker-${String(index+1).padStart(2,'0')}.webp`;document.body.appendChild(a);a.click();a.remove();await new Promise(r=>setTimeout(r,180))}

$('addWhatsAppBtn').onclick=async()=>{
 if(stickers.length<3){toast('WhatsApp requiere al menos 3 stickers');return}
 if(stickers.length>30){toast('El paquete admite hasta 30 stickers');return}
 if(!window.StickerBridge||!window.StickerBridge.addPack){
  toast('Para añadir a WhatsApp, abre esta herramienta desde la aplicación Android');
  return;
 }
 const payload={name:packName,publisher:packAuthor,stickers:stickers.map(s=>s.data)};
 try{
  $('addWhatsAppBtn').disabled=true;
  toast('Preparando paquete para WhatsApp…');
  window.StickerBridge.addPack(JSON.stringify(payload));
 }catch(err){toast('No se pudo preparar el paquete')}
 finally{$('addWhatsAppBtn').disabled=stickers.length<3}
};

$('exportAllBtn').onclick=async()=>{for(let i=0;i<stickers.length;i++)await downloadSticker(stickers[i],i);toast('Exportación iniciada. Revisa tus descargas.')};
async function shareSticker(s,index){
 try{const blob=await (await fetch(s.data)).blob();const file=new File([blob],`sticker-${index+1}.webp`,{type:'image/webp'});
  if(navigator.canShare&&navigator.canShare({files:[file]})){await navigator.share({files:[file],title:'Sticker'});return}
  await downloadSticker(s,index);toast('Sticker descargado para compartir')
 }catch(e){if(e.name!=='AbortError')toast('No se pudo compartir. Prueba descargándolo.')}
}
$('stickerGrid').addEventListener('click',e=>{if(e.target.tagName==='IMG'){const i=[...$('stickerGrid').querySelectorAll('img')].indexOf(e.target);shareSticker(stickers[i],i)}});
loadSaved();renderPack();render();updateOutputs();