/**
 * imageUpload.js - 이미지 업로드 미리보기 & 대표 사진 선택
 */

let accumulatedFiles = new DataTransfer();

function previewImages(input) {
  const previewList = document.getElementById('previewList');
  const uploadArea  = document.getElementById('uploadArea');

  Array.from(input.files).forEach(file => {
    if (accumulatedFiles.items.length < 10) accumulatedFiles.items.add(file);
  });
  input.files = accumulatedFiles.files;

  if (accumulatedFiles.items.length === 0) return;
  uploadArea.classList.add('img-upload-area-sm');
  renderPreviews();
}

function renderPreviews() {
  const previewList = document.getElementById('previewList');
  const uploadArea  = document.getElementById('uploadArea');
  previewList.innerHTML = '';

  const files = Array.from(accumulatedFiles.files);
  const currentThumb = parseInt(document.getElementById('thumbnailIndex').value) || 0;

  // 1단계: DOM 순서를 먼저 확정 (async 이전에 appendChild 완료)
  const imgEls = files.map((file, idx) => {
    const wrap = document.createElement('div');
    wrap.className = 'img-preview-item' + (idx === currentThumb ? ' is-thumbnail' : '');
    wrap.dataset.index = idx;

    const img = document.createElement('img');
    img.className = 'img-preview-thumb';
    img.alt = file.name;

    const delBtn = document.createElement('button');
    delBtn.type = 'button';
    delBtn.className = 'img-delete-btn';
    delBtn.textContent = '✕';
    delBtn.onclick = () => removeImage(idx);

    const badge = document.createElement('button');
    badge.type = 'button';
    badge.className = 'img-thumbnail-btn';
    badge.textContent = idx === currentThumb ? '대표 ✓' : '대표로';
    badge.onclick = () => setThumbnail(idx);

    wrap.appendChild(img);
    wrap.appendChild(delBtn);
    wrap.appendChild(badge);
    previewList.appendChild(wrap);
    return img;
  });

  // 2단계: 이미지 src는 나중에 채움 (DOM 순서는 이미 확정됨)
  files.forEach((file, idx) => {
    const reader = new FileReader();
    reader.onload = (e) => { imgEls[idx].src = e.target.result; };
    reader.readAsDataURL(file);
  });

  uploadArea.style.display = accumulatedFiles.items.length >= 10 ? 'none' : '';
}

function removeImage(removeIdx) {
  const input = document.getElementById('imgInput');
  const newDt = new DataTransfer();
  Array.from(accumulatedFiles.files).forEach((file, idx) => {
    if (idx !== removeIdx) newDt.items.add(file);
  });
  accumulatedFiles = newDt;
  input.files = accumulatedFiles.files;

  document.getElementById('thumbnailIndex').value = 0;
  document.getElementById('uploadArea').style.display = '';
  renderPreviews();
}

function setThumbnail(selectedIdx) {
  document.getElementById('thumbnailIndex').value = selectedIdx;
  document.querySelectorAll('#previewList .img-preview-item').forEach((item) => {
    const idx = parseInt(item.dataset.index);
    const btn = item.querySelector('.img-thumbnail-btn');
    if (idx === selectedIdx) {
      item.classList.add('is-thumbnail');
      btn.textContent = '대표 ✓';
    } else {
      item.classList.remove('is-thumbnail');
      btn.textContent = '대표로';
    }
  });
}
