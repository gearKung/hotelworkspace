<template>
  <div class="room-management-container">
    <header class="page-header">
      <h1 class="page-title">객실 관리</h1>
      <button @click="goToRegisterPage" class="btn-primary">＋ 새 객실 등록</button>
    </header>

    <div class="room-list-card">
      <table class="room-table">
        <thead>
          <tr>
            <th>객실명</th>
            <th>객실 타입</th>
            <th>기본 인원 / 최대 인원</th>
            <th>기본 요금</th>
            <th>상태</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="room in sampleRooms" :key="room.id">
            <td>{{ room.name }}</td>
            <td>{{ room.type }}</td>
            <td>{{ room.baseOccupancy }} / {{ room.maxOccupancy }}</td>
            <td>₩{{ room.price.toLocaleString() }}</td>
            <td>
              <span :class="['status-badge', room.status === '판매중' ? 'status-active' : 'status-inactive']">
                {{ room.status }}
              </span>
            </td>
            <td>
              <button class="btn-secondary btn-sm">수정</button>
              <button class="btn-danger btn-sm">삭제</button>
            </td>
          </tr>
          <tr v-if="sampleRooms.length === 0">
            <td colspan="6" class="no-data">등록된 객실이 없습니다. '새 객실 등록' 버튼을 눌러 추가해주세요.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OwnerRoom',
  data() {
    return {
      // 👇 [수정] sampleRooms를 빈 배열 [] 로 초기화합니다.
      // 나중에 실제 API로 데이터를 가져오기 전까지 오류를 방지합니다.
      sampleRooms: [
        { id: 1, name: '디럭스 더블룸', type: '더블', baseOccupancy: 2, maxOccupancy: 2, price: 150000, status: '판매중' },
        { id: 2, name: '프리미어 트윈룸', type: '트윈', baseOccupancy: 2, maxOccupancy: 3, price: 180000, status: '판매중' },
        { id: 3, name: '패밀리 스위트', type: '스위트', baseOccupancy: 4, maxOccupancy: 5, price: 250000, status: '판매 중지' },
      ],
    };
  },
  methods: {
    // 👇 메소드 이름 오타 수정: goToRegisterPag -> goToRegisterPage
    goToRegisterPage() {
      this.$router.push({ name: 'OwnerRoomRegister' });
    },
  },
};
</script>

<style scoped>
/* 스타일은 변경할 필요 없이 그대로 유지합니다. */
.room-management-container {
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

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #212529;
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
  transition: background-color 0.2s;
}
.btn-primary:hover {
  background-color: #4338ca;
}

.room-list-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #e9ecef;
}

.room-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
}

.room-table th {
  padding: 16px;
  border-bottom: 2px solid #dee2e6;
  font-size: 14px;
  font-weight: 700;
  color: #495057;
  background-color: #f8f9fa;
}

.room-table td {
  padding: 16px;
  border-bottom: 1px solid #e9ecef;
  font-size: 15px;
  vertical-align: middle;
}

.room-table tbody tr:last-child td {
  border-bottom: none;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 700;
}

.status-active {
  background-color: #d1fae5;
  color: #065f46;
}

.status-inactive {
  background-color: #fee2e2;
  color: #991b1b;
}

.btn-secondary, .btn-danger {
  border: 1px solid #dee2e6;
  background-color: #fff;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  margin-right: 8px;
  transition: background-color 0.2s, color 0.2s;
}
.btn-danger {
  color: #dc3545;
  border-color: #dc3545;
}
.btn-secondary:hover {
  background-color: #f1f3f5;
}
.btn-danger:hover {
  background-color: #dc3545;
  color: white;
}
.no-data {
    text-align: center;
    padding: 60px;
    color: #868e96;
}
</style>