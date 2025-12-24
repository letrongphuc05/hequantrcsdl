document.addEventListener("DOMContentLoaded", function() {
    loadStats();
    loadReviews();

    window.toggleProfileMenu = function(event) {
        event.stopPropagation();
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown) dropdown.classList.toggle('show');
    };

    window.addEventListener('click', function(event) {
        if (!event.target.closest('.admin-profile')) {
            const dropdown = document.getElementById('profileDropdown');
            if (dropdown && dropdown.classList.contains('show')) {
                dropdown.classList.remove('show');
            }
        }
    });
});

let allReviews = [];

async function loadStats() {
    try {
        const res = await fetch('/api/reviews/admin/stats');
        if (!res.ok) throw new Error('Failed to fetch stats');
        const stats = await res.json();

        document.getElementById('totalReviews').textContent = stats.totalReviews || 0;

        document.getElementById('avgCarRating').innerHTML = `${stats.avgCarRating || 0} <i class="fas fa-star"></i>`;
        document.getElementById('avgStaffRating').innerHTML = `${stats.avgStaffRating || 0} <i class="fas fa-star"></i>`;

        if (stats.carRatingDistribution) {
            renderRatingDistribution(stats.carRatingDistribution);
        }
    } catch (e) {
        console.error('Error loading stats:', e);
    }
}

async function loadReviews() {
    const container = document.getElementById('reviewsList');
    container.innerHTML = `<div class="empty-state"><i class="fas fa-spinner fa-spin"></i><h3>Đang tải dữ liệu...</h3></div>`;

    try {
        const res = await fetch('/api/reviews/admin/all');
        if (!res.ok) throw new Error("Lỗi tải dữ liệu");

        allReviews = await res.json();
        renderReviews(allReviews);
    } catch (e) {
        console.error('Error:', e);
        container.innerHTML = `
            <div class="empty-state" style="color: #e74c3c;">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Lỗi kết nối server</h3>
                <p>Vui lòng thử lại sau.</p>
            </div>
        `;
    }
}

function renderReviews(reviews) {
    const container = document.getElementById('reviewsList');

    if (!reviews || reviews.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-star" style="color: #ccc;"></i>
                <h3>Chưa có đánh giá nào</h3>
            </div>
        `;
        return;
    }

    container.innerHTML = reviews.map(review => {
        const initial = review.customerName ? review.customerName.charAt(0).toUpperCase() : '?';
        const dateStr = review.reviewDate ? new Date(review.reviewDate).toLocaleString('vi-VN') : 'N/A';

        let carStars = '';
        if (review.carRating) {
            carStars = `<div class="rating-item">
                            <span class="rating-label">Đánh giá xe</span>
                            <div class="stars-display">${renderStars(review.carRating)}</div>
                        </div>`;
        }

        let staffStars = '';
        if (review.staffRating) {
            staffStars = `<div class="rating-item">
                            <span class="rating-label">Đánh giá nhân viên</span>
                            <div class="stars-display">${renderStars(review.staffRating)}</div>
                        </div>`;
        }

        return `
            <div class="review-card">
                <div class="review-header">
                    <div class="customer-info">
                        <div class="customer-avatar">${initial}</div>
                        <div>
                            <div class="customer-name">${escapeHtml(review.customerName || 'Khách hàng')}</div>
                            <div class="review-date">${dateStr}</div>
                        </div>
                    </div>
                </div>

                <div class="ratings-row">
                    ${carStars}
                    ${staffStars}
                </div>

                <div class="review-details">
                    <div class="detail-item">
                        <i class="fas fa-car"></i>
                        <span>${escapeHtml(review.vehicleName || 'N/A')}</span>
                    </div>
                    <div class="detail-item">
                        <i class="fas fa-user-tie"></i>
                        <span>${escapeHtml(review.staffName || 'N/A')}</span>
                    </div>
                </div>

                ${review.comment ? `<div class="review-comment"><p>"${escapeHtml(review.comment)}"</p></div>` : ''}
            </div>
        `;
    }).join('');
}

function renderStars(rating) {
    let html = '';
    for (let i = 1; i <= 5; i++) {
        html += `<i class="fas fa-star ${i <= rating ? '' : 'empty'}"></i>`;
    }
    return html;
}

function renderRatingDistribution(distribution) {
    const container = document.getElementById('carRatingDistribution');
    if (!container) return;

    const total = distribution.reduce((a, b) => a + b, 0);
    let html = '';
    for (let i = 4; i >= 0; i--) {
        const count = distribution[i];
        const percentage = total > 0 ? (count / total * 100) : 0;
        html += `
            <div class="rating-bar">
                <span class="stars">${i + 1} sao</span>
                <div class="bar-container">
                    <div class="bar-fill" style="width: ${percentage}%"></div>
                </div>
                <span class="count">${count}</span>
            </div>
        `;
    }
    container.innerHTML = html;
}

function applyFilters() {
    let filtered = [...allReviews];
    const ratingFilter = document.getElementById('ratingFilter').value;
    if (ratingFilter !== 'all') {
        const rating = parseInt(ratingFilter);
        filtered = filtered.filter(r => r.carRating === rating || r.staffRating === rating);
    }
    const typeFilter = document.getElementById('typeFilter').value;
    if (typeFilter === 'car') filtered = filtered.filter(r => r.carRating && !r.staffRating);
    else if (typeFilter === 'staff') filtered = filtered.filter(r => !r.carRating && r.staffRating);
    else if (typeFilter === 'both') filtered = filtered.filter(r => r.carRating && r.staffRating);

    const sortFilter = document.getElementById('sortFilter').value;
    if (sortFilter === 'date-desc') filtered.sort((a, b) => new Date(b.reviewDate) - new Date(a.reviewDate));
    else if (sortFilter === 'date-asc') filtered.sort((a, b) => new Date(a.reviewDate) - new Date(b.reviewDate));
    else if (sortFilter === 'rating-high') filtered.sort((a, b) => (b.carRating || 0) - (a.carRating || 0));
    else if (sortFilter === 'rating-low') filtered.sort((a, b) => (a.carRating || 0) - (b.carRating || 0));

    renderReviews(filtered);
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}