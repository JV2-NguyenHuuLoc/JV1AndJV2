document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("productEditForm");
    if (!form) {
        return;
    }

    const productId = form.dataset.productId;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const payload = validateProductForm();
        if (!payload) {
            return;
        }

        try {
            const response = await fetch("/api/products/" + productId, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(payload)
            });

            if (response.status === 400) {
                applyServerErrors(await response.json());
                return;
            }
            if (!response.ok) {
                throw new Error("Request failed");
            }

            const product = await response.json();
            alert("Cập nhật sản phẩm thành công! ID: " + product.id + " — " + product.title);
            window.location.href = "/product_detail?id=" + product.id;
        } catch (error) {
            alert("Cập nhật sản phẩm thất bại. Vui lòng thử lại.");
        }
    });
});
