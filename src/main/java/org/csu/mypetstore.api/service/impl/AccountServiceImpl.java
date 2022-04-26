package org.csu.mypetstore.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.csu.mypetstore.api.common.CommonResponse;
import org.csu.mypetstore.api.entity.Account;
import org.csu.mypetstore.api.entity.BannerData;
import org.csu.mypetstore.api.entity.Profile;
import org.csu.mypetstore.api.entity.SignOn;
import org.csu.mypetstore.api.persistence.AccountMapper;
import org.csu.mypetstore.api.persistence.BannerDataMapper;
import org.csu.mypetstore.api.persistence.ProfileMapper;
import org.csu.mypetstore.api.persistence.SignOnMapper;
import org.csu.mypetstore.api.service.AccountService;
import org.csu.mypetstore.api.vo.AccountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("accountService")
@Slf4j
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private SignOnMapper signOnMapper;
    @Autowired
    private ProfileMapper profileMapper;
    @Autowired
    private BannerDataMapper bannerDataMapper;

    @Override
    public CommonResponse<AccountVO> getAccount(String username, String password) {
        // 构造查询器先去signon表中查
        QueryWrapper<SignOn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password); // 明文密码

        SignOn signOn = signOnMapper.selectOne(queryWrapper);
        if (signOn==null) {
            return CommonResponse.createForError("用户名或者密码不正确");
        }
        return getAccount(username);
    }

    @Override
    public CommonResponse<AccountVO> getAccount(String username) {
        Account account = accountMapper.selectById(username);
        Profile profile = profileMapper.selectById(username);
        if (account == null || profile == null) {
            return CommonResponse.createForError("获取用户信息失败");
        }
        BannerData bannerData = bannerDataMapper.selectById(profile.getFavouriteCategoryId());
        return CommonResponse.createForSuccess(accountToAccountVO(account, profile, bannerData));
    }

    @Override
    public CommonResponse usernameExist(String username) {
        try {
            Account account = accountMapper.selectById(username);
            if (account == null) {
                return CommonResponse.createForSuccessMessage("用户名不存在");
            }
            return CommonResponse.createForSuccessMessage("用户名已存在");
        } catch (Exception e) {
            log.error("用户名查询出错", e);
            return CommonResponse.createForError("用户名查询出错");
        }
    }

    @Override
    public CommonResponse insertAccount(String username, String password) {
        AccountVO accountVO = new AccountVO(username, password);
        Map<String, Object> datas = this.accountVOToPO(accountVO);
        Account account = (Account) datas.get("account");
        Profile profile = (Profile) datas.get("profile");
        SignOn signOn = (SignOn) datas.get("signon");

        try {
            accountMapper.insert(account);
            profileMapper.insert(profile);
            signOnMapper.insert(signOn); // 明文密码，未加密
            return CommonResponse.createForSuccessMessage("成功创建用户！", accountVO);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.createForError("用户创建失败！");
        }
    }

    @Override
    public CommonResponse updateAccount(AccountVO accountVO) {
        Map<String, Object> datas = this.accountVOToPO(accountVO);
        Account account = (Account) datas.get("account");
        Profile profile = (Profile) datas.get("profile");

        try {
            accountMapper.updateById(account);
            profileMapper.updateById(profile);
            return CommonResponse.createForSuccessMessage("用户信息更新成功！", accountVO);
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.createForError("用户信息更新失败！");
        }
    }

    @Override
    public CommonResponse updatePassword(String username, String curPw, String newPw) {
        QueryWrapper<SignOn> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", curPw);

        try {
            SignOn curSign = signOnMapper.selectOne(queryWrapper);
            if (curSign == null) {
                return CommonResponse.createForError("原密码不正确或用户不存在");
            }
            // 如果原密码与账户对的上
            curSign.setPassword(newPw);
            signOnMapper.updateById(curSign);
            return CommonResponse.createForSuccessMessage("用户密码修改成功！");
        } catch (Exception e) {
            e.printStackTrace();
            return CommonResponse.createForError("用户密码修改失败！");
        }
    }


    private AccountVO accountToAccountVO(Account account, Profile profile, BannerData bannerData) {

        AccountVO accountVO = new AccountVO();
        // account 注入
        accountVO.setUsername(account.getUsername());
        // 返回的对象密码清空处理保证安全
        accountVO.setPassword("");
        accountVO.setEmail(account.getEmail());
        accountVO.setFirstName(account.getFirstName());
        accountVO.setLastName(account.getLastName());
        accountVO.setStatus(account.getStatus());
        accountVO.setAddress1(account.getAddress1());
        accountVO.setAddress2(account.getAddress2());
        accountVO.setCity(account.getCity());
        accountVO.setState(account.getState());
        accountVO.setZip(account.getZip());
        accountVO.setCountry(account.getCountry());
        accountVO.setPhone(account.getPhone());

        // profile 注入
        accountVO.setLanguagePreference(profile.getLanguagePreference());
        accountVO.setListOption(profile.isListOption());
        accountVO.setBannerOption(profile.isBannerOption());

        if (profile.isBannerOption()) {
            accountVO.setFavouriteCategoryId(profile.getFavouriteCategoryId());
            // bannerdata 注入
            accountVO.setBannerName(bannerData.getBannerName());
        } else {
            accountVO.setFavouriteCategoryId("");
            accountVO.setBannerName("");
        }
        return accountVO;
    }


    private Map<String, Object> accountVOToPO(AccountVO accountVO) {
        Account account = new Account();
        account.setUsername(accountVO.getUsername());
        account.setEmail(accountVO.getEmail());
        account.setFirstName(accountVO.getFirstName());
        account.setLastName(accountVO.getLastName());
        account.setStatus(accountVO.getStatus());
        account.setAddress1(accountVO.getAddress1());
        account.setAddress2(accountVO.getAddress2());
        account.setCity(accountVO.getCity());
        account.setState(accountVO.getState());
        account.setZip(accountVO.getZip());
        account.setCountry(accountVO.getCountry());
        account.setPhone(accountVO.getPhone());

        Profile profile = new Profile();
        profile.setUsername(accountVO.getUsername());
        profile.setLanguagePreference(accountVO.getLanguagePreference());
        profile.setFavouriteCategoryId(accountVO.getFavouriteCategoryId());
        profile.setListOption(accountVO.isListOption());
        profile.setBannerOption(accountVO.isBannerOption());

        SignOn signOn = new SignOn();
        signOn.setUsername(accountVO.getUsername());
        signOn.setPassword(accountVO.getPassword());

        Map<String, Object> res = new HashMap<>();
        res.put("account", account);
        res.put("profile", profile);
        res.put("signon", signOn);
        return res;
    }
}
