/**
 * imageUpload.js - 이미지 업로드 미리보기 & 대표 사진 선택
 * 여러 번 파일 선택해도 누적되어 최대 5장까지 추가됨
 */

// DataTransfer로 파일 목록 누적 관리
let accumulatedFiles = new DataTransfer();

function previewImages(input) {
  const previewList = document.getElementById('previewList');
  const uploadArea  = document.getElementById('uploadArea');

  // 새로 선택한 파일들을 기존 목록에 누적 (최대 5장)
  Array.from(input.files).forEach(file => {
    if (accumulatedFiles.items.length < 5) {
      accumulatedFiles.items.add(file);
    }
  });

  // input에 누적된 파일 목록 반영
  input.files = accumulatedFiles.files;

  if (accumulatedFiles.items.length === 0) return;

  // 업로드 영역 축소
  uploadArea.classList.add('img-upload-area-sm');

  // 미리보기 전체 재렌더링
  previewList.innerHTML = '';
  Array.from(accumulatedFiles.files).forEach((file, idx) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const wrap = document.createElement('div');
      wrap.className = 'img-preview-item';
      wrap.dataset.index = idx;

      const img = document.createElement('img');
      img.src = e.target.result;
      img.className = 'img-preview-thumb';
      img.alt = file.name;

      // 삭제 버튼
      const delBtn = document.createElement('button');
      delBtn.type = 'button';
      delBtn.className = 'img-delete-btn';
      delBtn.textContent = '✕';
      delBtn.onclick = () => removeImage(idx);

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
      wrap.appendChild(delBtn);
      wrap.appendChild(badge);
      previewList.appendChild(wrap);
    };
    reader.readAsDataURL(file);
  });

  // 5장 다 채우면 업로드 영역 숨김
  if (accumulatedFiles.items.length >= 5) {
    uploadArea.style.display = 'none';
  }
}

function removeImage(removeIdx) {
  const input = document.getElementById('imgInput');
  const newDt = new DataTransfer();
  Array.from(accumulatedFiles.files).forEach((file, idx) => {
    if (idx !== removeIdx) newDt.items.add(file);
  });
  accumulatedFiles = newDt;
  input.files = accumulatedFiles.files;

  // 업로드 영역 다시 표시
  document.getElementById('uploadArea').style.display = '';

  // 미리보기 재렌더링
  previewImages({ files: new FileList() });
  const previewList = document.getElementById('previewList');
  previewList.innerHTML = '';
  Array.from(accumulatedFiles.files).forEach((file, idx) => {
    const reader = new FileReader();
    reader.onload = (e) => {
      const wrap = document.createElement('div');
      wrap.className = 'img-preview-item' + (idx === 0 ? ' is-thumbnail' : '');
      wrap.dataset.index = idx;
      const img = document.createElement('img');
      img.src = e.target.result;
      img.className = 'img-preview-thumb';
      const delBtn = document.createElement('button');
      delBtn.type = 'button';
      delBtn.className = 'img-delete-btn';
      delBtn.textContent = '✕';
      delBtn.onclick = () => removeImage(idx);
      const badge = document.createElement('button');
      badge.type = 'button';
      badge.className = 'img-thumbnail-btn';
      badge.textContent = idx === 0 ? '대표 ✓' : '대표로';
      badge.onclick = () => setThumbnail(idx);
      wrap.appendChild(img);
      wrap.appendChild(delBtn);
      wrap.appendChild(badge);
      previewList.appendChild(wrap);
    };
    reader.readAsDataURL(file);
  });
  document.getElementById('thumbnailIndex').value = 0;
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
