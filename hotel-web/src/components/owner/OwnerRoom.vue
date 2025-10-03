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
            <th>기본 / 최대 인원</th>
            <th>판매가 (1박)</th>
            <th>상태</th>
            <th>관리</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="room in rooms" :key="room.id">
            <td>{{ room.name }}</td>
            <td>{{ room.roomType }}</td>
            <td>{{ room.capacity }}</td>
            <td>{{ room.price }}</td>
            
            <td>
              <button class="btn-secondary btn-sm">수정</button>
              <button class="btn-danger btn-sm">삭제</button>
            </td>
          </tr>
          <tr v-if="rooms.length === 0">
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
      // 👇 [추가] 실제 객실 목록을 담을 배열
      rooms: [],
    };
  },
  methods: {
    // 👇 [수정] goToRegisterPag -> goToRegisterPage 오타 수정
    goToRegisterPage() {
      this.$router.push({ name: 'OwnerRoomRegister' });
    },
    // 👇 [추가] 백엔드 API를 호출하여 객실 목록을 가져오는 메소드
    async fetchRooms() {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          alert("로그인이 필요합니다.");
          this.$router.push('/login');
          return;
        }

        const response = await this.$axios.get('/api/owner/rooms', {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        });
        
        this.rooms = response.data; // API로부터 받은 데이터로 rooms 배열 업데이트

      } catch (error) {
        console.error("객실 목록을 불러오는 데 실패했습니다:", error);
        alert("객실 목록을 불러오는 중 오류가 발생했습니다.");
      }
    }
  },
  // 👇 [추가] 컴포넌트가 생성될 때 자동으로 객실 목록을 불러오도록 설정
  mounted() {
    this.fetchRooms();
  }
};
</script>

<style scoped>
/* 기존 스타일은 그대로 유지합니다. */
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