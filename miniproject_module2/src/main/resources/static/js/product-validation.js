function countWords(text) {
    const trimmed = text.trim();
    if (!trimmed) {
        return 0;
    }
    return trimmed.split(/\s+/).length;
}

function getProductFormData() {
    return {
        title: document.getElementById("title").value.trim(),
        description: document.getElementById("description").value.trim(),
        price: document.getElementById("price").value.trim(),
        category: document.getElementById("category").value.trim(),
        thumbnail: document.getElementById("thumbnail").value.trim(),
        brand: document.getElementById("brand").value.trim()
    };
}

function setFieldError(fieldId, message) {
    const input = document.getElementById(fieldId);
    const error = document.getElementById(fieldId + "Error");
    input.classList.add("is-invalid");
    error.textContent = message;
}

function clearFieldErrors(fieldIds) {
    fieldIds.forEach(function (fieldId) {
        const input = document.getElementById(fieldId);
        const error = document.getElementById(fieldId + "Error");
        input.classList.remove("is-invalid");
        error.textContent = "";
    });
}

function validateProductForm() {
    const fields = ["title", "description", "price", "category", "thumbnail", "brand"];
    clearFieldErrors(fields);

    const data = getProductFormData();
    let isValid = true;

    if (!data.title) {
        setFieldError("title", "Tên sản phẩm không được rỗng.");
        isValid = false;
    }

    if (!data.description) {
        setFieldError("description", "Mô tả không được rỗng.");
        isValid = false;
    } else if (countWords(data.description) > 255) {
        setFieldError("description", "Mô tả tối đa 255 từ.");
        isValid = false;
    }

    const price = Number(data.price);
    if (!data.price) {
        setFieldError("price", "Giá không được rỗng.");
        isValid = false;
    } else if (Number.isNaN(price) || price < 1 || price > 200) {
        setFieldError("price", "Giá phải từ 1 đến 200 USD.");
        isValid = false;
    }

    if (!data.category) {
        setFieldError("category", "Nhóm sản phẩm không được rỗng.");
        isValid = false;
    }

    if (!data.thumbnail) {
        setFieldError("thumbnail", "URL hình ảnh không được rỗng.");
        isValid = false;
    }

    if (!data.brand) {
        setFieldError("brand", "Thương hiệu không được rỗng.");
        isValid = false;
    }

    if (!isValid) {
        return null;
    }

    return {
        title: data.title,
        description: data.description,
        price: price,
        category: data.category,
        thumbnail: data.thumbnail,
        brand: data.brand
    };
}

// Hiển thị lỗi do server (Bean Validation) trả về dạng { tên trường: thông báo }
function applyServerErrors(errors) {
    if (!errors) {
        return;
    }
    Object.keys(errors).forEach(function (fieldId) {
        if (document.getElementById(fieldId)) {
            setFieldError(fieldId, errors[fieldId]);
        }
    });
}
