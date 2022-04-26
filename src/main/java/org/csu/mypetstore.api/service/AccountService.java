package org.csu.mypetstore.api.service;

import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.vo.AccountVO;

public interface AccountService {


    CommonResponse<AccountVO> getAccount(String username, String password);


    CommonResponse<AccountVO> getAccount(String username);


    CommonResponse usernameExist(String username);


    CommonResponse insertAccount(String username, String password);


    CommonResponse updateAccount(AccountVO accountVO);


    CommonResponse updatePassword(String username, String curPw, String newPw);
}
