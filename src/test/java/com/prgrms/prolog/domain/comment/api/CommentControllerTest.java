package com.prgrms.prolog.domain.comment.api;

import static com.prgrms.prolog.utils.TestUtils.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prgrms.prolog.config.RestDocsConfig;
import com.prgrms.prolog.domain.comment.dto.CommentDto;
import com.prgrms.prolog.domain.comment.service.CommentService;
import com.prgrms.prolog.domain.user.dto.UserDto;
import com.prgrms.prolog.global.jwt.JwtTokenProvider;
import com.prgrms.prolog.utils.TestUtils;

@SpringBootTest
@ExtendWith(RestDocumentationExtension.class)
@Import(RestDocsConfig.class)
class CommentControllerTest {

	private static final JwtTokenProvider jwtTokenProvider = JWT_TOKEN_PROVIDER;

	@Autowired
	RestDocumentationResultHandler restDocs;

	MockMvc mockMvc;

	@MockBean
	CommentService commentService;

	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	void setUpRestDocs(WebApplicationContext webApplicationContext,
		RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
			.apply(documentationConfiguration(restDocumentation))
			.alwaysDo(restDocs)
			.apply(springSecurity())
			.build();
	}

	@Test
	void commentSaveApiTest() throws Exception {
		UserDto.UserInfo userInfo = getUserInfo();
		JwtTokenProvider.Claims claims = JwtTokenProvider.Claims.from(userInfo.email(), USER_ROLE);
		CommentDto.CreateCommentRequest createCommentRequest = new CommentDto.CreateCommentRequest(
			TestUtils.getComment().getContent());

		when(commentService.save(createCommentRequest, userInfo.email(), 1L))
			.thenReturn(1L);

		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/v1/posts/{post_id}/comments", 1L)
				.header("token", jwtTokenProvider.createAccessToken(claims))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createCommentRequest)))
			.andExpect(status().isCreated())
			.andDo(restDocs.document(
				requestFields(
					fieldWithPath("content").description("댓글 내용")
				),
				responseBody()
			));
	}

	@Test
	void commentUpdateApiTest() throws Exception {
		UserDto.UserInfo userInfo = getUserInfo();
		JwtTokenProvider.Claims claims = JwtTokenProvider.Claims.from(userInfo.email(), USER_ROLE);

		CommentDto.UpdateCommentRequest updateCommentRequest = new CommentDto.UpdateCommentRequest(
			TestUtils.getComment().getContent() + "updated");

		when(commentService.update(updateCommentRequest, userInfo.email(), 1L))
			.thenReturn(1L);

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.patch("/api/v1/posts/{post_id}/comments/{id}", 1, 1)
				.header("token", jwtTokenProvider.createAccessToken(claims))
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateCommentRequest)))
			.andExpect(status().isOk())
			.andDo(restDocs.document(
				requestFields(
					fieldWithPath("content").description("수정된 댓글 내용")
				),
				responseBody()
			));
	}
}