<template>
  <div class="page-container">
    <header class="page-header">
      <h1 class="page-title">예약 관리</h1>
    </header>

    <div class="filter-bar">
      <select v-model="filters.roomType" @change="applyFilters">
        <option value="">모든 객실 타입</option>
        <option v-for="rt in roomTypes" :key="rt" :value="rt">{{ rt }}</option>
      </select>
      <input type="text" v-model="filters.guestName" placeholder="방문자 이름 검색" @input="applyFilters" />
      <select v-model="filters.status">
        <option value="">전체 상태</option> 
        <option value="COMPLETED">예약 완료</option>
        <option value="CANCELLED">예약 취소</option>
      </select>
      <select v-model="filters.resStatus">
        <option value="">모든 진행 상태</option>
        <option value="RESERVED">예약</option>
        <option value="CHECKED_IN">체크인</option>
        <option value="CHECKED_OUT">체크아웃</option>
      </select>
      <button @click="resetFilters" class="btn-secondary">초기화</button>
    </div>

    <main class="calendar-card">
      <FullCalendar ref="fullCalendar" :options="calendarOptions" />
    </main>

    <div v-if="listModal.visible" class="modal-overlay" @click.self="closeListModal">
      <div class="modal-content list-modal">
        <header class="modal-header">
          <h3>{{ listModal.date }} 예약 목록</h3>
          <button @click="closeListModal" class="close-button">&times;</button>
        </header>
        <ul class="reservation-list">
          <li v-for="event in listModal.events" :key="event.id" @click="openDetailModal(event.id)">
            <span class="list-item-color" :style="{ backgroundColor: event.color }"></span>
            {{ event.title }}
          </li>
        </ul>
      </div>
    </div>

    <div v-if="detailModal.visible" class="modal-overlay" @click.self="closeDetailModal">
      <div class="modal-content detail-modal">
        <header class="modal-header">
          <h3>예약 상세 정보</h3>
          <button @click="closeDetailModal" class="close-button">&times;</button>
        </header>
        <div v-if="detailModal.loading" class="loading-spinner"></div>
        <div v-if="detailModal.data" class="detail-grid">
          <div><label>호텔명</label><span>{{ detailModal.data.hotelName }}</span></div>
          <div><label>객실타입</label><span>{{ detailModal.data.roomType }}</span></div>
          <div><label>방문자 이름</label><span>{{ detailModal.data.guestName }}</span></div>
          <div><label>방문자 전화번호</label><span>{{ detailModal.data.guestPhone }}</span></div>
          <div><label>체크인</label><span>{{ detailModal.data.checkInDate }}</span></div>
          <div><label>체크아웃</label><span>{{ detailModal.data.checkOutDate }}</span></div>
          <div><label>인원</label><span>성인 {{ detailModal.data.numAdult }}명, 아동 {{ detailModal.data.numKid }}명</span></div>
          <div><label>예약 상태</label><span :class="detailModal.data.reservationStatus.toLowerCase()">{{ detailModal.data.reservationStatus }}</span></div>
          <div class="full-width"><label>체크인/아웃 상태</label><span :class="detailModal.data.resStatus.toLowerCase()">{{ detailModal.data.resStatus }}</span></div>
        </div>
        <div v-if="detailModal.data" class="modal-actions">
          <button v-if="canCheckIn" @click="updateResStatus('check-in')" class="btn-primary">체크인</button>
          <button v-if="canCancelCheckIn" @click="updateResStatus('cancel-check')" class="btn-secondary">체크인 취소</button>
          <button v-if="canCheckOut" @click="updateResStatus('check-out')" class="btn-success">체크아웃</button>
          <button v-if="canCancelCheckOut" @click="updateResStatus('cancel-check')" class="btn-secondary">체크아웃 취소</button>
          <button v-if="canCancelReservation" @click="updateResStatus('cancel')" class="btn-danger">예약 취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import listPlugin from '@fullcalendar/list';
import interactionPlugin from '@fullcalendar/interaction';
import axios from '@/api/http';

export default {
  name: 'OwnerReservation',
  components: { FullCalendar },
  setup() {
    const fullCalendar = ref(null);
    const allEvents = ref([]);
    const roomTypes = ref(['스위트룸', '디럭스룸', '스탠다드룸', '싱글룸', '트윈룸']);
    const today = new Date().toISOString().split('T')[0];

    const filters = reactive({
      roomType: '',
      guestName: '',
      status: 'COMPLETED',
      resStatus: '', // 체크인/아웃 상태 필터 추가
    });

    const listModal = reactive({ visible: false, date: '', events: [] });
    const detailModal = reactive({ visible: false, loading: false, data: null, id: null });

    const filteredEvents = computed(() => {
        if (!allEvents.value) return [];
        return allEvents.value.filter(event => {
            const detail = event.extendedProps;
            if (!detail) return false;
            
            const guestNameMatch = filters.guestName === '' || event.title.toLowerCase().includes(filters.guestName.toLowerCase());
            const statusMatch = filters.status === '' || event.status === filters.status;
            const resStatusMatch = filters.resStatus === '' || detail.resStatus === filters.resStatus; // resStatus 필터링 추가
            const roomTypeMatch = filters.roomType === '' || detail.roomName.includes(filters.roomType);
            
            return guestNameMatch && statusMatch && resStatusMatch && roomTypeMatch;
        });
    });
    
    const calendarOptions = reactive({
      plugins: [dayGridPlugin, listPlugin, interactionPlugin],
      initialView: 'dayGridMonth',
      headerToolbar: {
        left: 'prev,next today',
        center: 'title',
        right: 'dayGridMonth,listMonth'
      },
      locale: 'ko',
      events: [],
      dayMaxEvents: 5,
      eventClick: (info) => {
        openDetailModal(info.event.id);
      },
      dateClick: (info) => {
        const calendarApi = fullCalendar.value.getApi();
        const eventsOnDate = calendarApi.getEvents().filter(e => {
            const start = e.startStr.split('T')[0];
            const end = new Date(e.end - 1).toISOString().split('T')[0];
            return info.dateStr >= start && info.dateStr <= end;
        });

        if (eventsOnDate.length > 0) {
            listModal.date = info.dateStr;
            listModal.events = eventsOnDate.map(e => ({
              id: e.id,
              title: e.title,
              color: e.backgroundColor
            }));
            listModal.visible = true;
        }
      },
    });

    async function fetchAllReservations() {
      try {
        const token = localStorage.getItem('token');
        if(!token) throw new Error("Token not found");

        const { data } = await axios.get('/api/owner/reservations', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        allEvents.value = data;
        applyFilters();
      } catch (error) {
        console.error("예약 로딩 실패:", error);
      }
    }
    
    function applyFilters() {
      calendarOptions.events = filteredEvents.value;
    }
    
    watch(filters, applyFilters);

    function closeListModal() { listModal.visible = false; }
    function closeDetailModal() { detailModal.visible = false; detailModal.data = null; }

    async function openDetailModal(id) {
      if(listModal.visible) listModal.visible = false;
      detailModal.visible = true;
      detailModal.loading = true;
      detailModal.id = id;
      try {
        const token = localStorage.getItem('token');

        const { data } = await axios.get(`/api/owner/reservations/${id}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        detailModal.data = data;
      } catch(e) {
        alert('상세 정보를 불러오지 못했습니다.');
        closeDetailModal();
      } finally {
        detailModal.loading = false;
      }
    }

    function resetFilters() {
      filters.roomType = '';
      filters.guestName = '';
      filters.status = 'COMPLETED';
      filters.resStatus = '';
    }

    const canCheckIn = computed(() => detailModal.data && detailModal.data.resStatus === 'RESERVED' && detailModal.data.checkInDate === today);
    const canCancelCheckIn = computed(() => detailModal.data && detailModal.data.resStatus === 'CHECKED_IN');
    const canCheckOut = computed(() => detailModal.data && detailModal.data.resStatus === 'CHECKED_IN' && detailModal.data.checkOutDate === today);
    const canCancelCheckOut = computed(() => detailModal.data && detailModal.data.resStatus === 'CHECKED_OUT');
    const canCancelReservation = computed(() => detailModal.data && detailModal.data.reservationStatus !== 'CANCELLED' && detailModal.data.checkInDate >= today);

    async function updateResStatus(action) {
      if(!confirm(`정말로 이 작업을 실행하시겠습니까?`)) return;
      try {
        const token = localStorage.getItem('token');
        await axios.put(`/api/owner/reservations/${detailModal.id}/${action}`, {}, {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        alert('상태가 성공적으로 변경되었습니다.');
        await openDetailModal(detailModal.id);
        await fetchAllReservations();
      } catch(e) {
        alert(e.response?.data?.message || '상태 변경에 실패했습니다.');
      }
    }

    onMounted(fetchAllReservations);
    
    return {
      fullCalendar,
      calendarOptions,
      filters,
      roomTypes,
      listModal,
      detailModal,
      closeListModal,
      openDetailModal,
      closeDetailModal,
      resetFilters,
      applyFilters,
      canCheckIn,
      canCancelCheckIn,
      canCheckOut,
      canCancelCheckOut,
      canCancelReservation,
      updateResStatus,
    };
  },
};
</script>

<style scoped>
.page-container { padding: 40px; background-color: #f8f9fa; height: 100%; }
.page-header { margin-bottom: 24px; }
.page-title { font-size: 28px; font-weight: 800; color: #212529; }
.filter-bar { display: flex; gap: 12px; margin-bottom: 24px; align-items: center; flex-wrap: wrap; }
.filter-bar input, .filter-bar select { padding: 10px 14px; border-radius: 8px; border: 1px solid #d1d5db; font-size: 14px; }
.date-separator { color: #6b7280; }
.calendar-card { background: #fff; border-radius: 12px; padding: 32px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05); }
:deep(.fc-button-primary) { background-color: #4f46e5 !important; border-color: #4f46e5 !important; }
:deep(.fc-daygrid-day.fc-day-today) { background-color: #eef2ff; }
:deep(.fc-event) { cursor: pointer; }

/* 모달 공통 */
.modal-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal-content { background: white; border-radius: 12px; width: 90%; box-shadow: 0 10px 25px rgba(0,0,0,0.1); }
.modal-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 24px; border-bottom: 1px solid #e5e7eb; }
.modal-header h3 { margin: 0; font-size: 20px; }
.close-button { background: none; border: none; font-size: 28px; cursor: pointer; color: #9ca3af; }

/* 리스트 모달 */
.list-modal { max-width: 400px; }
.reservation-list { list-style: none; padding: 16px; margin: 0; max-height: 60vh; overflow-y: auto; }
.reservation-list li { cursor: pointer; padding: 12px 16px; border-radius: 6px; display: flex; align-items: center; gap: 12px; font-weight: 500; }
.reservation-list li:hover { background-color: #f3f4f6; }
.list-item-color { width: 10px; height: 10px; border-radius: 50%; }

/* 상세 모달 */
.detail-modal { max-width: 600px; }
.detail-grid { padding: 24px; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.detail-grid > div { display: flex; flex-direction: column; }
.detail-grid label { font-size: 13px; color: #6b7280; margin-bottom: 4px; }
.detail-grid span { font-weight: 500; }
.detail-grid .full-width { grid-column: 1 / -1; }
.modal-actions { display: flex; justify-content: flex-end; gap: 12px; padding: 20px 24px; border-top: 1px solid #e5e7eb; background-color: #f9fafb; border-bottom-left-radius: 12px; border-bottom-right-radius: 12px;}
.btn-primary, .btn-secondary, .btn-danger, .btn-success { padding: 10px 20px; border: none; border-radius: 8px; font-weight: 600; cursor: pointer; }
.btn-primary { background-color: #4f46e5; color: white; }
.btn-secondary { background-color: #e5e7eb; color: #374151; }
.btn-danger { background-color: #ef4444; color: white; }
.btn-success { background-color: #22c55e; color: white; }
span.completed { color: #2563eb; font-weight: bold; }
span.cancelled { color: #9ca3af; font-weight: bold; }
span.reserved { color: #f59e0b; font-weight: bold; }
span.checked_in { color: #22c55e; font-weight: bold; }
span.checked_out { color: #1e40af; font-weight: bold; }
</style>