<template>
  <div class="page-container">
    <header class="page-header">
      <div>
        <button @click="goBack" class="btn-back">←</button>
        <h1 class="page-title">새 객실 등록</h1>
      </div>
      </header>

    <main class="form-container">
      <form @submit.prevent="handleSubmit">
        <div class="form-section">
          <h3>기본 정보</h3>
          <div class="form-grid">
            <div class="form-group span-2">
              <label for="roomName">객실명</label>
              <input type="text" id="roomName" v-model="room.name" placeholder="예: 프리미어 디럭스 더블룸" required>
            </div>
            <div class="form-group">
              <label for="roomType">객실 타입</label>
              <select id="roomType" v-model="room.roomType" required>
                <option disabled value="">선택하세요</option>
                <option>스위트룸</option>
                <option>디럭스룸</option>
                <option>스탠다드룸</option>
                <option>싱글룸</option>
                <option>트윈룸</option>
              </select>
            </div>
            <div class="form-group">
              <label for="price">판매가 (1박)</label>
              <input type="text" id="price" v-model="formattedPrice" placeholder="숫자만 입력" required>
            </div>
            <div class="form-group">
              <label for="roomSize">방 크기</label>
              <div class="input-with-unit">
                <input type="number" id="roomSize" v-model.number="room.size" placeholder="숫자만 입력">
                <span>m²</span>
              </div>
            </div>
            <div class="form-group">
                <label for="roomCount">보유 객실 수</label>
                <input type="number" id="roomCount" v-model.number="room.roomCount" required>
            </div>
            <div class="form-group">
              <label for="capacityMin">기본 인원</label>
              <input type="number" id="capacityMin" v-model.number="room.capacityMin" required>
            </div>
            <div class="form-group">
              <label for="capacityMax">최대 인원</label>
              <input type="number" id="capacityMax" v-model.number="room.capacityMax" required>
            </div>
            <div class="form-group">
              <label for="checkInTime">체크인 시간</label>
              <input type="time" id="checkInTime" v-model="room.checkInTime" required>
            </div>
            <div class="form-group">
              <label for="checkOutTime">체크아웃 시간</label>
              <input type="time" id="checkOutTime" v-model="room.checkOutTime" required>
            </div>
          </div>
        </div>

        <div class="form-section">
          <h3>객실 편의시설</h3>
          <div class="amenities-grid">
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.smoke"> 금연 객실</label>
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.bath"> 욕조</label>
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.aircon"> 에어컨</label>
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.wifi"> 무료 Wi-Fi</label>
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.freeWater"> 무료 생수</label>
            <label class="amenity-label"><input type="checkbox" v-model="room.facilities.hasWindow"> 창문</label>
          </div>
        </div>

        <div class="form-section">
          <h3>객실 사진</h3>
          <p class="description">첫 번째 사진이 대표 이미지로 사용됩니다. 드래그 앤 드랍으로 순서를 변경할 수 있습니다.</p>
          <div class="image-upload-container">
            <div class="image-preview-list">
              <div v-for="(image, index) in images" :key="image.id" class="image-preview-item" draggable="true"
                   @dragstart="onDragStart(index)" @dragover.prevent @drop="onDrop(index)" @dragend="dragEnd">
                <img :src="image.preview" :alt="image.file.name">
                <button type="button" class="remove-image-btn" @click="removeImage(index)">&times;</button>
                <span class="image-order-badge">{{ index + 1 }}</span>
              </div>
              <label class="image-upload-box">
                <span>+<br>사진 추가</span>
                <input type="file" multiple @change="handleImageUpload" accept="image/*">
              </label>
            </div>
          </div>
        </div>
        
        <div class="form-footer">
          <button type="submit" class="btn-primary" :disabled="isSubmitting">
            {{ isSubmitting ? '저장 중...' : '저장하기' }}
          </button>
        </div>
      </form>
    </main>
  </div>
</template>

<script>
export default {
  name: 'OwnerRoomRegister',
  data() {
    return {
      isSubmitting: false,
      room: {
        name: '',
        roomType: '',
        price: null,
        size: null,
        roomCount: 1,
        capacityMin: 2,
        capacityMax: 2,
        checkInTime: '15:00',
        checkOutTime: '11:00',
        facilities: {
          smoke: true,
          bath: false,
          aircon: true,
          wifi: true,
          freeWater: true,
          hasWindow: true,
        },
      },
      images: [],
      dragIndex: null,
    };
  },
  computed: {
    formattedPrice: {
      get() {
        if (this.room.price === null) return '';
        return this.room.price.toLocaleString('ko-KR');
      },
      set(value) {
        const numericValue = parseInt(value.replace(/[^0-9]/g, ''), 10);
        this.room.price = isNaN(numericValue) ? null : numericValue;
      }
    }
  },
  methods: {
    goBack() {
      this.$router.push({ name: 'OwnerRoom' });
    },
    handleImageUpload(event) {
      const files = Array.from(event.target.files);
      files.forEach(file => {
        this.images.push({
          id: Date.now() + Math.random(),
          file: file,
          preview: URL.createObjectURL(file)
        });
      });
      event.target.value = null;
    },
    removeImage(index) {
      // 미리보기 URL 메모리 해제
      URL.revokeObjectURL(this.images[index].preview);
      this.images.splice(index, 1);
    },
    onDragStart(index) {
      this.dragIndex = index;
    },
    onDrop(dropIndex) {
      if (this.dragIndex === null || this.dragIndex === dropIndex) return;
      const draggedItem = this.images.splice(this.dragIndex, 1)[0];
      this.images.splice(dropIndex, 0, draggedItem);
    },
    dragEnd() {
      this.dragIndex = null;
    },
    async handleSubmit() {
      if (this.isSubmitting) return;
      this.isSubmitting = true;

      const formData = new FormData();
      
      const roomRequestData = {
        ...this.room,
        facilities: {
          ...this.room.facilities,
          bath: this.room.facilities.bath ? 1 : 0
        }
      };
      formData.append('roomRequest', new Blob([JSON.stringify(roomRequestData)], { type: "application/json" }));
      
      this.images.forEach(imageObj => {
        formData.append('images', imageObj.file);
      });

      try {
        const token = localStorage.getItem('token');
        if (!token) {
          alert("로그인이 필요합니다.");
          this.$router.push('/login');
          return;
        }

        await this.$axios.post('/api/owner/rooms', formData, {
          headers: {
            'Authorization': `Bearer ${token}`
            // 'Content-Type'은 axios가 FormData를 보낼 때 자동으로 설정하므로 명시하지 않습니다.
          }
        });
        
        alert(`'${this.room.name}' 객실이 성공적으로 등록되었습니다.`);
        this.goBack();

      } catch (error) {
        console.error("객실 등록 실패:", error.response || error);
        alert("객실 등록에 실패했습니다. 입력 내용을 확인하거나 다시 시도해주세요.");
      } finally {
        this.isSubmitting = false;
      }
    }
  },
  // 컴포넌트가 사라질 때 메모리 누수 방지를 위해 미리보기 URL들을 해제합니다.
  beforeUnmount() {
    this.images.forEach(image => URL.revokeObjectURL(image.preview));
  }
};
</script>

<style scoped>
/* 기존 스타일 코드는 그대로 유지합니다. */
.page-container {
  padding: 40px;
  background-color: #f8f9fa;
  height: 100%;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}
.page-header > div {
  display: flex;
  align-items: center;
  gap: 16px;
}
.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #212529;
  margin: 0;
}
.btn-back {
    background: #fff;
    border: 1px solid #dee2e6;
    width: 40px;
    height: 40px;
    border-radius: 50%;
    font-size: 20px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;
}
.btn-back:hover {
    background-color: #f1f3f5;
}
.form-container {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.form-section {
  margin-bottom: 40px;
}
.form-section h3 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
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
}
input, select {
  padding: 0px 16px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 16px;
  height: 48px;
}
.input-with-unit {
    display: flex;
    align-items: center;
    border: 1px solid #ced4da;
    border-radius: 8px;
    padding-right: 16px;
}
.input-with-unit input {
    border: none;
    flex-grow: 1;
}
.input-with-unit span {
    color: #868e96;
}
.combined-group {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 16px;
  align-items: flex-end;
}
.sub-group {
  display: flex;
  flex-direction: column;
}
.toggle-switch {
  display: flex;
  height: 48px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
}
.toggle-switch button {
  flex: 1;
  border: none;
  background: #fff;
  cursor: pointer;
  font-weight: 600;
  transition: background .2s, color .2s;
}
.toggle-switch button.active {
  background: #4f46e5;
  color: white;
}
.amenities-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}
.amenity-label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px;
  border-radius: 8px;
  cursor: pointer;
}
.amenity-label input {
    width: 18px;
    height: 18px;
}
.description {
    font-size: 14px;
    color: #868e96;
    margin-top: -16px;
    margin-bottom: 16px;
}
.image-upload-container {
    background: #f8f9fa;
    padding: 16px;
    border-radius: 8px;
}
.image-preview-list {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
}
.image-upload-box {
  border: 2px dashed #ced4da;
  border-radius: 8px;
  width: 150px;
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
  cursor: pointer;
  position: relative;
  color: #868e96;
}
.image-upload-box input[type="file"] {
  display: none;
}
.image-preview-item {
  position: relative;
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.image-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.remove-image-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: none;
  background: rgba(0,0,0,0.6);
  color: white;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  line-height: 1;
}
.image-order-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: rgba(0,0,0,0.6);
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  font-weight: bold;
}
.btn-primary {
  background-color: #4f46e5;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 16px;
  cursor: pointer;
}
.btn-primary:disabled {
    background-color: #a5b4fc;
    cursor: not-allowed;
}

/* 👇 [추가] 폼 하단 저장 버튼을 위한 스타일 */
.form-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #e9ecef;
  display: flex;
  justify-content: flex-end;
}

</style>