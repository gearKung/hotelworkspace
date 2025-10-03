<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <header class="modal-header">
        <h2>새 객실 등록</h2>
        <button class="close-button" @click="$emit('close')">&times;</button>
      </header>
      <main class="modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-grid">
            <div class="form-group span-2">
              <label for="roomName">객실명</label>
              <input type="text" id="roomName" v-model="room.name" placeholder="예: 디럭스 더블룸" required>
            </div>
            <div class="form-group">
              <label for="roomType">객실 타입</label>
              <select id="roomType" v-model="room.type" required>
                <option disabled value="">선택하세요</option>
                <option>싱글</option>
                <option>더블</option>
                <option>트윈</option>
                <option>스위트</option>
                <option>패밀리</option>
              </select>
            </div>
            <div class="form-group">
              <label for="price">기본 요금 (1박)</label>
              <input type="number" id="price" v-model.number="room.price" placeholder="숫자만 입력" required>
            </div>
            <div class="form-group">
              <label for="baseOccupancy">기본 인원</label>
              <input type="number" id="baseOccupancy" v-model.number="room.baseOccupancy" required>
            </div>
            <div class="form-group">
              <label for="maxOccupancy">최대 인원</label>
              <input type="number" id="maxOccupancy" v-model.number="room.maxOccupancy" required>
            </div>
            <div class="form-group span-2">
              <label>객실 사진 등록</label>
              <div class="image-upload-box">
                <p>클릭하거나 파일을 드래그하여 업로드하세요.</p>
                <input type="file" multiple @change="handleImageUpload">
              </div>
            </div>
          </div>
          <footer class="modal-footer">
            <button type="button" class="btn-secondary" @click="$emit('close')">취소</button>
            <button type="submit" class="btn-primary">저장하기</button>
          </footer>
        </form>
      </main>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AddRoomModal',
  data() {
    return {
      room: {
        name: '',
        type: '',
        price: null,
        baseOccupancy: 2,
        maxOccupancy: 2,
        images: []
      }
    };
  },
  methods: {
    handleImageUpload(event) {
      this.room.images = Array.from(event.target.files);
      // TODO: 이미지 미리보기 또는 실제 업로드 로직 추가
    },
    handleSubmit() {
      // TODO: 실제 백엔드 API 호출 로직으로 교체해야 합니다.
      console.log('등록할 객실 정보:', this.room);
      alert(`${this.room.name} 객실 정보가 저장되었습니다! (현재는 UI만 구현)`);
      this.$emit('close'); // 성공 후 모달 닫기
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 700px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
}
.modal-header {
  padding: 24px 32px;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.modal-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}
.close-button {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #868e96;
}
.modal-body {
  padding: 32px;
  max-height: 70vh;
  overflow-y: auto;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.form-group {
  display: flex;
  flex-direction: column;
}
.form-group.span-2 {
  grid-column: span 2;
}
label {
  margin-bottom: 8px;
  font-weight: 600;
  font-size: 14px;
  color: #495057;
}
input, select {
  padding: 12px 16px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 16px;
}
input:focus, select:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 2px rgba(79, 70, 229, 0.25);
}
.image-upload-box {
  border: 2px dashed #ced4da;
  border-radius: 8px;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  position: relative;
}
.image-upload-box p {
  margin: 0;
  color: #868e96;
}
.image-upload-box input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
.modal-footer {
  padding: 24px 32px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-primary, .btn-secondary {
    border: none;
    padding: 12px 20px;
    border-radius: 8px;
    font-weight: 700;
    font-size: 16px;
    cursor: pointer;
    transition: background-color 0.2s;
}
.btn-primary {
  background-color: #4f46e5;
  color: white;
}
.btn-primary:hover {
  background-color: #4338ca;
}
.btn-secondary {
  background-color: #e9ecef;
  color: #495057;
}
.btn-secondary:hover {
  background-color: #dee2e6;
}
</style>