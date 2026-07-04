document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("productAddForm");
    if (!form) {
        return;
    }

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const payload = validateProductForm();
        if (!payload) {
            return;
        }

        try {
            const response = await fetch("/api/products", {
                method: "POST",
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
            alert("Thêm sản phẩm thành công! ID: " + product.id + " — " + product.title);
            window.location.href = "/product_detail?id=" + product.id;
        } catch (error) {
            alert("Thêm sản phẩm thất bại. Vui lòng thử lại.");
        }
    });
});
