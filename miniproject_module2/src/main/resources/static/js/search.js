document.addEventListener("DOMContentLoaded", function () {
    const searchInput = document.getElementById("searchKeyword");
    const searchBtn = document.getElementById("searchBtn");

    if (!searchInput || !searchBtn) {
        return;
    }

    function performSearch() {
        const keyword = searchInput.value.trim();
        if (keyword.length <= 2) {
            alert("Từ khóa tìm kiếm phải lớn hơn 2 ký tự.");
            return;
        }
        window.location.href = "/product_search?keyword=" + encodeURIComponent(keyword);
    }

    searchBtn.addEventListener("click", performSearch);

    searchInput.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            performSearch();
        }
    });
});
