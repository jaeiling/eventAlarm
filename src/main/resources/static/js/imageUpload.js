/**
 * imageUpload.js - 이미지 업로드 미리보기 & 대표 사진 선택
 */

function previewImages(input) {
  const previewList = document.getElementById('previewList');
  const uploadArea  = document.getElementById('uploadArea');
  previewList.innerHTML = '';

  const files = Array.from(input.files).slice(0, 5); // 최대 5장
  if (files.length === 0) return;

  // 업로드 영역 축소
  uploadArea.classList.add('img-upload-area-sm');

  files.forEach((file, idx) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const wrap = document.createElement('div');
      wrap.className = 'img-preview-item';
      wrap.dataset.index = idx;

      const img = document.createElement('img');
      img.src = e.target.result;
      img.className = 'img-preview-thumb';
      img.alt = file.name;

      // 대표 뱃지
      const badge = document.createElement('button');
      badge.type = 'button';
      badge.className = 'img-thumbnail-btn';
      badge.textContent = idx === 0 ? '대표 ✓' : '대표로';
      badge.onclick = () => setThumbnail(idx);

      if (idx === 0) {
        wrap.classList.add('is-thumbnail');
        document.getElementById('thumbnailIndex').value = 0;
      }

      wrap.appendChild(img);
      wrap.appendChild(badge);
      previewList.appendChild(wrap);
    };
    reader.readAsDataURL(file);
  });
}

function setThumbnail(selectedIdx) {
  document.getElementById('thumbnailIndex').value = selectedIdx;

  document.querySelectorAll('#previewList .img-preview-item').forEach((item, idx) => {
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
