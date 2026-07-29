package com.alibaba.ticketsystem.service;

import com.alibaba.ticketsystem.dto.TicketCategoryRequest;
import com.alibaba.ticketsystem.entity.TicketCategory;
import com.alibaba.ticketsystem.mapper.AfterSalePolicyMapper;
import com.alibaba.ticketsystem.mapper.FaqMapper;
import com.alibaba.ticketsystem.mapper.TicketCategoryMapper;
import com.alibaba.ticketsystem.mapper.TicketMapper;
import com.alibaba.ticketsystem.utils.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TicketCategoryServiceTest {

    private final TicketCategoryMapper categoryMapper = mock(TicketCategoryMapper.class);
    private final TicketMapper ticketMapper = mock(TicketMapper.class);
    private final FaqMapper faqMapper = mock(FaqMapper.class);
    private final AfterSalePolicyMapper policyMapper = mock(AfterSalePolicyMapper.class);
    private final AgentGroupService groupService = mock(AgentGroupService.class);
    private final TicketCategoryService service = new TicketCategoryService(
            categoryMapper, groupService, ticketMapper, faqMapper, policyMapper);

    @Test
    void existingCategoryCodeCannotBeRenamed() {
        when(categoryMapper.selectById(1L)).thenReturn(category("REFUND"));
        TicketCategoryRequest request = request("LOGISTICS");

        assertStatus(() -> service.update(1L, request), HttpStatus.CONFLICT);
    }

    @Test
    void referencedCategoryCannotBeDeleted() {
        when(categoryMapper.selectById(1L)).thenReturn(category("REFUND"));
        when(ticketMapper.selectCount(any())).thenReturn(1L);

        assertStatus(() -> service.delete(1L), HttpStatus.CONFLICT);
    }

    private TicketCategory category(String code) {
        TicketCategory category = new TicketCategory();
        category.setId(1L);
        category.setCategoryCode(code);
        category.setCategoryName("退款");
        category.setEnabled(1);
        category.setDeleted(0);
        return category;
    }

    private TicketCategoryRequest request(String code) {
        TicketCategoryRequest request = new TicketCategoryRequest();
        request.setCategoryCode(code);
        request.setCategoryName("分类");
        request.setEnabled(1);
        return request;
    }

    private void assertStatus(Runnable action, HttpStatus status) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getStatus()).isEqualTo(status));
    }
}
